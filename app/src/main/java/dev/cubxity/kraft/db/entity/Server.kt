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

package dev.cubxity.kraft.db.entity

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "servers")
data class Server(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "name") override var name: String,
    @ColumnInfo(name = "server_host") override var host: String,
    @ColumnInfo(name = "server_port") override var port: Int = 25565
) : Parcelable, dev.cubxity.kraft.entity.Server {
    override val isLazy: Boolean
        get() = false

    override val description: String
        get() = "$host:$port"

    companion object {
        fun create(name: String, serverHost: String, serverPort: Int) =
            Server(null, name, serverHost, serverPort)
    }
}