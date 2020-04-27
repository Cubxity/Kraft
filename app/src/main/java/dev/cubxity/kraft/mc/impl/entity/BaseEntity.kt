/*
 *     Kraft: Lightweight Minecraft client for Android featuring modules support and other task automation
 *     Copyright (C) 2020  Cubxity
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cubxity.kraft.mc.impl.entity

import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata
import com.github.steveice10.mc.protocol.data.game.entity.type.`object`.ObjectData
import dev.cubxity.kraft.mc.entitiy.Entity
import java.util.*

open class BaseEntity(override val entityId: Int, override val uuid: UUID) : Entity {
    override var data: ObjectData? = null

    override var x: Double = 0.0
    override var y: Double = 0.0
    override var z: Double = 0.0
    override var pitch: Float = 0F
    override var yaw: Float = 0F
    override var velocityX: Double = 0.0
    override var velocityY: Double = 0.0
    override var velocityZ: Double = 0.0

    override var metadata: Array<EntityMetadata> = emptyArray()
}