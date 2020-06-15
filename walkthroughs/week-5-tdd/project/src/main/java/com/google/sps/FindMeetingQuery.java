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
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Setting up arrays first.
    List<TimeRange> busyTimes = new ArrayList<>();
    List<TimeRange> mergedBusyTimes = new ArrayList<>();
    List<TimeRange> validTimes = new ArrayList<>();

    // Only get the TimeRanges where required attendees are busy.
    Collection<String> requiredAttendees = request.getAttendees();
    for (Event event : events) {
      if (eventContainsRequiredAttendees(event, requiredAttendees)) {
        busyTimes.add(event.getWhen());
      }
    }

    // Sort the busyTimes list by end time.
    Collections.sort(busyTimes, TimeRange.ORDER_BY_START);

    // Create a stack for merging all the busy times together, traversing the 
    // list sequentially. Using array list as a stack because I'll need it as
    // an array list right after.
    if (busyTimes.isEmpty()) {
      if (TimeRange.WHOLE_DAY.duration() >= request.getDuration()) {
        validTimes.add(TimeRange.WHOLE_DAY);
      }
      return validTimes;
    }
    mergedBusyTimes.add(busyTimes.get(0));
    for (int i = 1; i < busyTimes.size(); i++) {
      TimeRange top = mergedBusyTimes.get(mergedBusyTimes.size()-1);
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

    // From the merged times, getting all the free times that have a valid duration
    // for the meeting. Also adding slots from the beginning of the day to the
    // first event and from the last event to the end of the day.
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
    validTimes.removeIf(time -> time.duration() < request.getDuration());
    return validTimes;
  }

  private boolean eventContainsRequiredAttendees(Event event, Collection<String> requiredAttendees) {
    return !Collections.disjoint(event.getAttendees(), requiredAttendees);
  }
}
