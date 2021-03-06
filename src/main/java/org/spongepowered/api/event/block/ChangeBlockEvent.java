/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.api.event.block;

import com.google.common.collect.Lists;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.world.TargetWorldEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.function.Predicate;

/**
 * Base event for when {@link BlockState}s at {@link Location}s are being
 * changed.
 */
public interface ChangeBlockEvent extends TargetWorldEvent, Cancellable {

    /**
     * Gets a list of the {@link Transaction}s for this event. If a
     * transaction is requested to be marked as "invalid",
     * {@link Transaction#setValid(boolean)} can be used.
     *
     * @return The unmodifiable list of transactions
     */
    List<Transaction<BlockSnapshot>> getTransactions();

    /**
     * Applies the provided {@link Predicate} to the {@link List} of
     * {@link Transaction}s from {@link #getTransactions()} such that
     * any time that {@link Predicate#test(Object)} returns <code>false</code>
     * on the location of the {@link Transaction}, the {@link Transaction} is
     * marked as "invalid" and will not apply post event.
     *
     * <p>{@link Transaction#getOriginal()} is used to get the {@link Location}</p>
     *
     * @param predicate The predicate to use for filtering
     * @return The transactions for which the predicate returned <code>false</code>
     */
    default List<Transaction<BlockSnapshot>> filter(Predicate<Location<World>> predicate) {
        List<Transaction<BlockSnapshot>> invalidatedTransactions = Lists.newArrayList();
        for (Transaction<BlockSnapshot> transaction: this.getTransactions()) {
            if (!predicate.test(transaction.getOriginal().getLocation().get())) {
                transaction.setValid(false);
                invalidatedTransactions.add(transaction);
            }
        }
        return invalidatedTransactions;
    }

    /**
     * Invalidates the list as such that all {@link Transaction}s are
     * marked as "invalid" and will not apply post event.
     */
    default void filterAll() {
        for (Transaction<BlockSnapshot> transaction: this.getTransactions()) {
            transaction.setValid(false);
        }
    }

    /**
     * Called when specific {@link BlockType}s have a notion of "decaying"
     * for various reasons such that the changes are always caused by
     * themselves.
     */
    interface Decay extends ChangeBlockEvent {}

    /**
     * Called when a {@link BlockType} decides to "grow" either other
     * blocks or itself or both. Usually considered to be plants or crops.
     */
    interface Grow extends ChangeBlockEvent {}

    /**
     * Called when {@link BlockState}s at {@link Location}s are
     * being broke. This usually occurs, whenever a {@link BlockState} changes
     * to {@link BlockTypes#AIR}
     *
     */
    interface Break extends ChangeBlockEvent {}

    /**
     * Called when one or more {@link BlockType}s are added to the world.
     *
     */
    interface Place extends ChangeBlockEvent {}

    /**
     * Called when one or more {@link BlockType}s are modified in the world.
     *
     */
    interface Modify extends ChangeBlockEvent {}

    /**
     * Called when there are multiple block changes due to a
     * {@link BlockType} having "ticked", in which the {@link Cause} will
     * have a {@link BlockSnapshot}, or, in the case that an {@link Entity}
     * has "ticked", in which the {@link Cause} will have an {@link Entity},
     * or, in the case that a {@link TileEntity} "ticked", the {@link Cause}
     * will have the {@link TileEntity}.
     *
     * <p>The {@link Cause} may contain {@link Event}s, such as {@link Break},
     * {@link Place}, and {@link Modify}. These events may be cancelled,
     * or have their transactions modified, just like normal events.</p>
     *
     * <p>The idea is that  a block, entity, or tile entity "ticks" in which
     * during that "tick", they make different types of block changes. If the
     * block change is purely one type then the corresponding event is thrown
     * instead. For example, If the block change is purely "placing" of
     * blocks, the {@link Place} event would be thrown instead.</p>
     *
     * <p>For example, a piston extension would cause this event to be fired.
     * A piston extension involves multiple distinct transactions -
     * the piston head moving, and the adjacent block being set in a new
     * position.</p>
     *
     * Note: This event is fired after processing all other
     * ChangeBlockEvent's.
     */
    interface Post extends ChangeBlockEvent {}
}
