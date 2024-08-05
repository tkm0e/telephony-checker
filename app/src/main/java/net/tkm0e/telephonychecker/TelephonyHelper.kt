/*
 * Copyright 2024 Takumi Oe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.tkm0e.telephonychecker

import android.telephony.TelephonyManager

object TelephonyHelper {
    fun getSimStateName(state: Int): String {
        val result = when (state) {
            TelephonyManager.SIM_STATE_ABSENT -> {
                "ABSENT"
            }
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> {
                "PIN_REQUIRED"
            }
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> {
                "PUK_REQUIRED"
            }
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> {
                "NETWORK_LOCKED"
            }
            TelephonyManager.SIM_STATE_READY -> {
                "READY"
            }
            TelephonyManager.SIM_STATE_NOT_READY -> {
                "NOT_READY"
            }
            TelephonyManager.SIM_STATE_PERM_DISABLED -> {
                "PERM_DISABLED"
            }
            TelephonyManager.SIM_STATE_CARD_IO_ERROR -> {
                "CARD_IO_ERROR"
            }
            TelephonyManager.SIM_STATE_CARD_RESTRICTED -> {
                "CARD_RESTRICTED"
            }
            10 -> {
                "LOADED"
            }
            11 -> {
                "PRESENT"
            }
            TelephonyManager.SIM_STATE_UNKNOWN -> {
                "UNKNOWN"
            }
            else -> {
                "N/A"
            }
        }
        return result.plus("($state)")
    }

    fun getDataStateName(state: Int): String {
        val result = when (state) {
            TelephonyManager.DATA_DISCONNECTED -> {
                "DISCONNECTED"
            }
            TelephonyManager.DATA_CONNECTING -> {
                "CONNECTING"
            }
            TelephonyManager.DATA_CONNECTED -> {
                "CONNECTED"
            }
            TelephonyManager.DATA_SUSPENDED -> {
                "SUSPENDED"
            }
            TelephonyManager.DATA_DISCONNECTING -> {
                "DISCONNECTING"
            }
            TelephonyManager.DATA_HANDOVER_IN_PROGRESS -> {
                "HANDOVER_IN_PROGRESS"
            }
            TelephonyManager.DATA_UNKNOWN -> {
                "UNKNOWN"
            }
            else -> {
                "N/A"
            }
        }
        return result.plus("($state)")
    }

    fun getDataNetworkTypeName(type: Int): String {
        val result = when (type) {
            TelephonyManager.NETWORK_TYPE_GPRS ->
                "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE ->
                "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS ->
                "UMTS"
            TelephonyManager.NETWORK_TYPE_HSDPA ->
                "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA ->
                "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA ->
                "HSPA"
            TelephonyManager.NETWORK_TYPE_CDMA ->
                "CDMA"
            TelephonyManager.NETWORK_TYPE_EVDO_0 ->
                "CDMA - EvDo rev. 0"
            TelephonyManager.NETWORK_TYPE_EVDO_A ->
                "CDMA - EvDo rev. A"
            TelephonyManager.NETWORK_TYPE_EVDO_B ->
                "CDMA - EvDo rev. B"
            TelephonyManager.NETWORK_TYPE_1xRTT ->
                "CDMA - 1xRTT"
            TelephonyManager.NETWORK_TYPE_LTE ->
                "LTE"
            TelephonyManager.NETWORK_TYPE_EHRPD ->
                "CDMA - eHRPD"
            TelephonyManager.NETWORK_TYPE_IDEN ->
                "iDEN"
            TelephonyManager.NETWORK_TYPE_HSPAP ->
                "HSPA+"
            TelephonyManager.NETWORK_TYPE_GSM ->
                "GSM"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA ->
                "TD_SCDMA"
            TelephonyManager.NETWORK_TYPE_IWLAN ->
                "IWLAN"
            19 ->
                "LTE_CA"
            TelephonyManager.NETWORK_TYPE_NR ->
                "NR"
            TelephonyManager.NETWORK_TYPE_UNKNOWN ->
                "UNKNOWN"
            else ->
                "N/A"
        }
        return result.plus("($type)")
    }
}