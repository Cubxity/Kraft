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
@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(
        entity = Account::class,
        parentColumns = ["uuid"],
        childColumns = ["account_uuid"]
    )]
)
data class Session(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "account_uuid") val accountUUID: String,
    @ColumnInfo(name = "server_host") var serverHost: String,
    @ColumnInfo(name = "server_port") var serverPort: Int = 25565
) : Parcelable {
    companion object {
        fun create(name: String, account: Account, serverHost: String, serverPort: Int) =
            SessionWithAccount(Session(null, name, account.uuid, serverHost, serverPort), account)
    }
}