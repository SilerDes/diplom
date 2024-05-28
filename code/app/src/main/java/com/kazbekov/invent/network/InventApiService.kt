package com.kazbekov.invent.network

import com.kazbekov.invent.main.data.antoher.ChangeVersion
import com.kazbekov.invent.main.data.employee.RemoteEmployees
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPositions
import com.kazbekov.invent.main.data.employee.RemotePassword
import com.kazbekov.invent.main.data.session.RemoteCreatedSession
import com.kazbekov.invent.main.data.session.RemoteSessions
import com.kazbekov.invent.main.data.antoher.RemoteTimestamp
import com.kazbekov.invent.main.data.inventory_item.RemoteInventoryItems
import com.kazbekov.invent.main.data.inventory_item.RemoteItems
import com.kazbekov.invent.main.data.post.InventoryItemsPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface InventApiService {

    //AUTH

    @FormUrlEncoded
    @POST("login.php")
    fun auth(
        @Field("code") code: Int,
        @Field("password") password: String
    ): Call<InventResponse.SuccessfulLoginResponse>

    //Employee

    @FormUrlEncoded
    @POST("signup.php")
    fun signup(
        @Field("employee_code") employeeCode: Int,
        @Field("first_name") firstName: String,
        @Field("second_name") lastName: String,
        @Field("password") password: String,
        @Field("trusted_status_id") trustedStatusId: Int = 1,
        @Field("admin_code") adminCode: Int
    ): Call<Unit>

    @GET("employee.php")
    fun getEmployeeList(@Query("by") by: Int): Call<RemoteEmployees>

    @GET("employee.php")
    fun getEmployee(
        @Query("by") by: Int,
        @Query("s") code: Int
    ): Call<RemoteEmployees>

    @DELETE("employee.php")
    fun deleteEmployee(
        @Query("by") by: Int,
        @Query("d") toDelete: Int
    ): Call<Unit>

    //Без пароля
    @FormUrlEncoded
    @PUT("employee.php")
    fun updateEmployee(
        @Field("by") by: Int, //Код сотрудника, кем инициирован запрос
        @Field("updatable") u: Int, //Код сотрудника, кого необходимо обновить
        @Field("first_name") firstName: String,
        @Field("second_name") secondName: String,
        @Field("trusted_status_id") trustedStatusId: Int
    ): Call<Unit>

    //С паролем
    @FormUrlEncoded
    @PUT("employee.php")
    fun updateEmployee(
        @Field("by") by: Int, //Код сотрудника, кем инициирован запрос
        @Field("updatable") u: Int, //Код сотрудника, кого необходимо обновить
        @Field("first_name") firstName: String,
        @Field("second_name") secondName: String,
        @Field("trusted_status_id") trustedStatusId: Int,
        @Field("password") password: String
    ): Call<Unit>


    //TODO реализовать этот запрос (на сервере все готово)
    @GET("changes.php")
    fun getLastChangeVersion(@Query("change_id") changeItem: Int): Call<ChangeVersion>

    //InventoryPosition

    @GET("availabilityPositionTitle.php")
    fun checkAvailabilityTitle(
        @Query("by") by: Int,
        @Query("requiredTitle") title: String
    ): Call<Unit>

    @FormUrlEncoded
    @POST("inventoryPosition.php")
    fun uploadInventoryPosition(
        @Field("by") by: Int,
        @Field("title_official") titleOfficial: String,
        @Field("title_non_official") titleNonOfficial: String,
        @Field("encoded_image") encodedImage: String
    ): Call<Unit>

    @GET("inventoryPosition.php")
    fun getInventoryPositions(): Call<RemoteInventoryPositions>

    @GET("inventoryPosition.php")
    fun searchInventoryPositions(@Query("s") s: String): Call<RemoteInventoryPositions>

    @DELETE("inventoryPosition.php")
    fun deleteInventoryPosition(
        @Query("by") by: Int,
        @Query("d") toDelete: Int
    ): Call<Unit>

    @PUT("inventoryPosition.php")
    @FormUrlEncoded
    fun updateInventoryPosition(
        @Field("by") by: Int,
        @Field("updatable") u: Int,
        @Field("title_official") titleOfficial: String,
        @Field("title_user") titleUser: String
    ): Call<Unit>

    @PUT("inventoryPosition.php")
    @FormUrlEncoded
    fun updateInventoryPosition(
        @Field("by") by: Int,
        @Field("updatable") u: Int,
        @Field("title_official") titleOfficial: String,
        @Field("title_user") titleUser: String,
        @Field("encoded_image") encodedImage: String
    ): Call<Unit>

    //Password

    @GET("password.php")
    fun requestPassword(
        @Query("by") by: Int,
        @Query("r") requested: Int
    ): Call<RemotePassword>

    //Session

    @POST("session.php")
    fun createSession(@Query("by") by: Int): Call<RemoteCreatedSession>

    @POST("session.php")
    fun stopSession(
        @Query("by") by: Int,
        @Query("session_id") sessionId: Int
    ): Call<RemoteTimestamp>

    @DELETE("session.php")
    fun deleteSession(
        @Query("by") by: Int,
        @Query("session_id") sessionId: Int
    ): Call<Unit>

    @GET("session.php")
    fun getSessions(@Query("by") by: Int): Call<RemoteSessions>

    //InventoryItem

    @GET("inventoryItem.php")
    fun getInventoryItems(
        @Query("by") by: Int,
        @Query("session_id") sessionId: Int
    ): Call<RemoteInventoryItems>

    @PUT("inventoryItem.php")
    fun updateInventoryItemState(
        @Query("id") id: Int,
        @Query("count") count: Int
    ): Call<Unit>

    @DELETE("inventoryItem.php")
    fun deleteInventoryItem(
        @Query("by") by: Int,
        @Query("id") id: Int
    ): Call<Unit>

    @Headers("Content-Type: application/json")
    @POST("inventoryItem.php")
    fun createInventoryItems(
        @Query("by") by: Int,
        @Body inventoryItems: InventoryItemsPost
    ): Call<Unit>

    //For User

    @GET("sessionUser.php")
    fun getUserSessions(
        @Query("by") by: Int
    ): Call<RemoteSessions>

    @GET("inventoryItemUser.php")
    fun getUserInventoryItems(
        @Query("by") by: Int,
        @Query("session_id") sessionId: Int
    ): Call<RemoteItems>
}