/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.interactor

import com.moez.QKSMS.model.Message
import com.moez.QKSMS.repository.MessageRepository
import javax.inject.Inject

class RetrySending @Inject constructor(
    private val messageRepo: MessageRepository
) : Interactor<Long>() {

    override suspend fun execute(params: Long) {
        val message = messageRepo.getMessage(params)
                ?.takeIf { it.isSms() } // TODO: Support resending failed MMS
                ?: return

        // We don't want to touch the supplied message on another thread in case it's a live realm
        // object, so copy the required fields into a new object that is safe to pass around threads
        val messageCopy = Message().apply {
            id = message.id
            type = message.type
            address = message.address
            body = message.body
            subId = message.subId
        }

        messageRepo.markSending(messageCopy.id)
        messageRepo.sendSms(messageCopy)
    }

}
