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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> busyTimes = new ArrayList<>();
    Collection<String> requiredAttendies = request.getAttendees();
    // Only get the TimeRanges where required attendees are busy.
    for (Event event : events) {
      if (eventContainsRequiredAttendees(event, requiredAttendies)) {
        busyTimes.add(event.getWhen());
      }
    }
    // Sort the busyTimes list by end time.
    Collections.sort(busyTimes, TimeRange.ORDER_BY_END);
    return null;
  }

  private boolean eventContainsRequiredAttendees(Event event, Collection<String> requiredAttendies) {
    return !Collections.disjoint(event.getAttendees(), requiredAttendies);
  }
}
