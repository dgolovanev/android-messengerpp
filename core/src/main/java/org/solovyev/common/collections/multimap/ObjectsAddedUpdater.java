/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.common.collections.multimap;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

public final class ObjectsAddedUpdater<V> implements ThreadSafeMultimap.ListUpdater<V> {

	@Nonnull
	private final Collection<V> newObjects;

	public ObjectsAddedUpdater(@Nonnull Collection<V> newObjects) {
		this.newObjects = newObjects;
	}

	@Nullable
	@Override
	public List<V> update(@Nonnull List<V> values) {
		if (values == ThreadSafeMultimap.NO_VALUE) {
			return null;
		} else {
			List<V> result = null;
			for (V newObject : newObjects) {
				if (!Iterables.contains(values, newObject)) {
					if (result == null) {
						result = ThreadSafeMultimap.copy(values);
					}
					result.add(newObject);
				}
			}
			return result;
		}
	}
}
