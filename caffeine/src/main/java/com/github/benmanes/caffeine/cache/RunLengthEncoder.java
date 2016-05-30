/*
 * Copyright 2016 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * @author ben.manes@gmail.com (Ben Manes)
 */
final class RunLengthEncoder<E> {
  private Object[] array;
  private int[] count;

  private int arrayIndex;
  private int countIndex;

  public RunLengthEncoder(int initialCapacity) {
    array = new Object[initialCapacity];
    count = new int[initialCapacity];
  }

  public void add(@Nonnull E e) {
    requireNonNull(e);
    if (array[arrayIndex] == null) {
      array[arrayIndex] = e;
      count[countIndex]++;
    } else if (array[arrayIndex] == e) {
      count[countIndex]++;
    } else {
      arrayIndex++;
      countIndex++;
      if (arrayIndex == array.length) {
        int capacity = array.length << 1;
        array = Arrays.copyOf(array, capacity);
        count = Arrays.copyOf(count, capacity);
      }
      array[arrayIndex] = e;
      count[countIndex] = 1;
    }
  }

  public void drainTo(@Nonnull RunLengthConsumer<E> consumer) {
    requireNonNull(consumer);

    if (array[0] == null) {
      return;
    }
    for (int i = 0; i <= arrayIndex; i++) {
      @SuppressWarnings("unchecked")
      E e = (E) array[i];
      array[i] = null;

      int amount = count[i];
      count[i] = 0;

      requireNonNull(e);
      consumer.accept(e, amount);
    }
    arrayIndex = 0;
    countIndex = 0;
  }

  interface RunLengthConsumer<E> {
    void accept(@Nonnull E e, @Nonnegative int count);
  }
}
