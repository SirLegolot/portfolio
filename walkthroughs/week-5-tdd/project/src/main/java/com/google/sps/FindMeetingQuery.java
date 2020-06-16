// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class FindMeetingQuery {
  // Simply checks if optional attendees can go, otherwise considers only the 
  // required attendees.
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long requestDuration = request.getDuration();
    Collection<String> requiredAttendees = request.getAttendees();

    // All attendees contains both required and optional attendies.
    Collection<String> allAttendees = new HashSet<>();
    allAttendees.addAll(requiredAttendees);
    allAttendees.addAll(request.getOptionalAttendees());

    Collection<TimeRange> queryWithOptionalAttendees = queryHelper(events, allAttendees, requestDuration);
    if (queryWithOptionalAttendees.isEmpty() && !requiredAttendees.isEmpty()) {
      return queryHelper(events, requiredAttendees, requestDuration);
    }
    return queryWithOptionalAttendees;
  }

  // This helper function returns the available times for a meeting given the attendees
  // and duration of the meeting. All attendees passed in to the function are assumed as 
  // required to attend. 
  public Collection<TimeRange> queryHelper(Collection<Event> events, Collection<String> requiredAttendees, long requestDuration) {
    List<TimeRange> busyTimes = new ArrayList<>();
    
    // Only get the TimeRanges where required attendees are busy.
    for (Event event : events) {
      if (eventContainsRequiredAttendees(event, requiredAttendees)) {
        busyTimes.add(event.getWhen());
      }
    }

    // Sort the busyTimes list by start time.
    Collections.sort(busyTimes, TimeRange.ORDER_BY_START);

    // If there are no busy times, the whole day is free.
    if (busyTimes.isEmpty()) {
      if (TimeRange.WHOLE_DAY.duration() >= requestDuration) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
      }
      return Arrays.asList();
    }

    // Merge the overlapping busy times together.
    List<TimeRange> mergedBusyTimes = mergeBusyTimes(busyTimes);
    
    // From the busy times, get all the valid available times for the meeting.
    return getValidTimes(mergedBusyTimes, requestDuration);
  }

  private boolean eventContainsRequiredAttendees(Event event, Collection<String> requiredAttendees) {
    return !Collections.disjoint(event.getAttendees(), requiredAttendees);
  }

  // Given a list of busy times sorted by start date, merge overlapping intervals
  // together. This method uses a stack to merge the times together sequentially.
  // Using array list as a stack because I'll need it as an array list right after.
  // We are also ensured that busyTimes is not empty, because it is checked right 
  // before in the queryHelper main code.
  private List<TimeRange> mergeBusyTimes(List<TimeRange> busyTimes) {
    List<TimeRange> mergedBusyTimes = new ArrayList<>();
    mergedBusyTimes.add(busyTimes.get(0));
    for (int i = 1; i < busyTimes.size(); i++) {
      TimeRange top = mergedBusyTimes.get(mergedBusyTimes.size()-1); // top of stack
      TimeRange cur = busyTimes.get(i); 
      // If the time interval overlaps, merge the intervals together.
      if (top.overlaps(cur)) {
        int newStart = Math.min(top.start(), cur.start());
        int newEnd = Math.max(top.end(), cur.end());
        int newDuration = newEnd - newStart;
        mergedBusyTimes.remove(top);
        mergedBusyTimes.add(TimeRange.fromStartDuration(newStart, newDuration));
      }
      else {
        mergedBusyTimes.add(cur);
      }
    }
    return mergedBusyTimes;
  }

  // From the merged times, get all the free times that have a valid duration
  // for the meeting. Also add slots from the beginning of the day to the
  // first event and from the last event to the end of the day.
  private Collection<TimeRange> getValidTimes(List<TimeRange> mergedBusyTimes, long requestDuration) {
    List<TimeRange> validTimes = new ArrayList<>();
    int firstDuration = mergedBusyTimes.get(0).start() - TimeRange.START_OF_DAY;
    validTimes.add(TimeRange.fromStartDuration(TimeRange.START_OF_DAY, firstDuration));
    // Creating slots for all times between consecutive merged events.
    for (int i = 0; i < mergedBusyTimes.size() - 1; i++) {
      TimeRange curTimeSlot = mergedBusyTimes.get(i);
      TimeRange nextTimeSlot = mergedBusyTimes.get(i+1);
      int newStart = curTimeSlot.end();
      int newDuration = nextTimeSlot.start() - newStart;
      validTimes.add(TimeRange.fromStartDuration(newStart, newDuration));
    }
    int lastStart = mergedBusyTimes.get(mergedBusyTimes.size()-1).end();
    validTimes.add(TimeRange.fromStartEnd(lastStart, TimeRange.END_OF_DAY, true));

    // Remove all intervals that are smaller than the request duration.
    validTimes.removeIf(time -> time.duration() < requestDuration);
    return validTimes;
  }
}
