@file:Suppress("unused")

package com.objectfanatics.commons.android.view.instancestate

import android.annotation.TargetApi
import android.os.Parcelable
import android.view.View
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import kotlin.reflect.KCallable
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

// ---------------------------------------------------------
// common for onSaveInstanceState and onRestoreInstanceState
// ---------------------------------------------------------

@Parcelize
data class FrozenState(
    val superState: Parcelable?,
    val thisState: List<@RawValue Any?>
) : Parcelable

// ---------------------------------------------------------
// for onSaveInstanceState
// ---------------------------------------------------------

// using 'inline' since this function is caller sensitive.
@TargetApi(26)
inline fun <reified T> T.onSaveInstanceState(vararg valueOrValueRetrievers: Any?): Parcelable =
    onSaveInstanceState(valueOrValueRetrievers.toList())

// using 'inline' since this function is caller sensitive.
@TargetApi(26)
inline fun <reified T> T.onSaveInstanceState(valueOrValueRetrievers: List<Any?>): Parcelable =
    onSaveInstanceState(superOnSaveInstanceState(), valueOrValueRetrievers = valueOrValueRetrievers)

fun onSaveInstanceState(superState: Parcelable?, vararg valueOrValueRetrievers: Any?): FrozenState =
    onSaveInstanceState(superState, valueOrValueRetrievers.toList())

fun onSaveInstanceState(superState: Parcelable?, valueOrValueRetrievers: List<Any?>): FrozenState =
    FrozenState(superState, valueOrValueRetrievers.map { getValue(it) })

// ---------------------------------------------------------
// for onRestoreInstanceState
// ---------------------------------------------------------

// using 'inline' since this function is caller sensitive.
@TargetApi(26)
inline fun <reified T> T.onRestoreInstanceState(frozenState: Parcelable, vararg restoreValues: Any?) {
    onRestoreInstanceState(frozenState, restoreValues.toList())
}

// using 'inline' since this function is caller sensitive.
@TargetApi(26)
inline fun <reified T> T.onRestoreInstanceState(frozenState: Parcelable, restoreValues: List<Any?>) =
    superOnRestoreInstanceState(restoreThisStateAndReturnSuperState(frozenState, restoreValues))

fun restoreThisStateAndReturnSuperState(frozenState: Parcelable, vararg restoreValues: Any?): Parcelable? {
    return restoreThisStateAndReturnSuperState(frozenState, restoreValues.toList())
}

fun restoreThisStateAndReturnSuperState(frozenState: Parcelable, restoreValues: List<Any?>): Parcelable? {
    restoreValues.forEachIndexed { index, restoreValue -> setValue(restoreValue, (frozenState as FrozenState).thisState[index]) }
    return (frozenState as FrozenState).superState
}

// ---------------------------------------------------------
// for calling super
// ---------------------------------------------------------

// Using 'inline' since 'MethodHandles.lookup()' is caller sensitive.
@TargetApi(26)
inline fun <reified T> T.superOnSaveInstanceState(): Parcelable {
    val methodName = "onSaveInstanceState"
    val methodType = MethodType.methodType(Parcelable::class.java)
    return superMethod<T>(methodName, methodType).invokeWithArguments(this) as Parcelable
}

// Using 'inline' since 'MethodHandles.lookup()' is caller sensitive.
@TargetApi(26)
inline fun <reified T> T.superOnRestoreInstanceState(state: Parcelable?) {
    val methodName = "onRestoreInstanceState"
    val methodType = MethodType.methodType(Void::class.javaPrimitiveType, Parcelable::class.java)
    superMethod<T>(methodName, methodType).invokeWithArguments(this, state)
}

@TargetApi(26)
inline fun <reified T> superMethod(methodName: String, methodType: MethodType?): MethodHandle =
    MethodHandles.lookup().findSpecial(
        View::class.java,
        methodName,
        methodType,
        T::class.java
    )

// ---------------------------------------------------------
// misc
// ---------------------------------------------------------

private fun getValue(valueOrValueRetriever: Any?): Any? =
    when (valueOrValueRetriever) {
        is KProperty<*> -> valueOrValueRetriever.getter.call()
        is KCallable<*> -> valueOrValueRetriever.call()
        is Function<*>  -> valueOrValueRetriever::class.java.getMethod("invoke").invoke(valueOrValueRetriever)
        else            -> valueOrValueRetriever
    }

private fun setValue(restoreValue: Any?, value: Any?) =
    when (restoreValue) {
        is KMutableProperty<*> -> restoreValue.setter.call(value)
        is KCallable<*>        -> restoreValue.call(value)
        is Function1<*, *>     -> restoreValue::class.java.getMethod("invoke", Any::class.java).invoke(restoreValue, value)
        else                   -> throw IllegalArgumentException("restoreValue.javaClass = ${restoreValue?.javaClass?.name}")
    }