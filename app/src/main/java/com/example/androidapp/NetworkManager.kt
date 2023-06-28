package com.example.androidapp

import io.grpc.Grpc
import io.grpc.Status
import io.grpc.ManagedChannelBuilder
import io.grpc.InsecureChannelCredentials
import io.grpc.okhttp.internal.Platform.logger
import io.grpc.stub.StreamObserver
import org.cs446_35.wastenot.InventoryServiceGrpc
import org.cs446_35.wastenot.InventoryServiceOuterClass.GetInventoryRequest
import org.cs446_35.wastenot.InventoryServiceOuterClass.GetInventoryResponse
import org.cs446_35.wastenot.InventoryServiceOuterClass.Item
import org.cs446_35.wastenot.RecipeServiceGrpc
import org.cs446_35.wastenot.RecipeServiceOuterClass.Recipe
import org.cs446_35.wastenot.RecipeServiceOuterClass.SearchRecipesByNameRequest
import org.cs446_35.wastenot.RecipeServiceOuterClass.SearchRecipesByNameResponse


class NetworkManager constructor(addr: String){
    private val channel = Grpc.newChannelBuilder(addr, InsecureChannelCredentials.create()).build()
    private val inventorySvc = InventoryServiceGrpc.newBlockingStub(channel);
    private val recipeSvc = RecipeServiceGrpc.newBlockingStub(channel);

//     private class RecipePrinter : StreamObserver<SearchRecipesByNameResponse> {
//
//        override fun onNext(resp: SearchRecipesByNameResponse) {
//            print(resp.recipesList)
//        }
//
//        override fun onError(t: Throwable) {
//            val status = Status.fromThrowable(t);
//            print("ERRRROR")
//            print(status)
//        }
//
//
//        override fun onCompleted() {
//            print("Finished Stream");
//        }
//    }
//
//    private class InvPrinter : StreamObserver<GetInventoryResponse> {
//
//        override fun onNext(resp: GetInventoryResponse) {
//            print(resp.itemsList)
//        }
//
//        override fun onError(t: Throwable) {
//            val status = Status.fromThrowable(t);
//            print("ERRRROR")
//            print(status)
//        }
//
//
//        override fun onCompleted() {
//            print("Finished Stream");
//        }
//    }

    fun getRecipes(): List<Recipe> {
//        var obs = RecipePrinter()
        val resp = recipeSvc.searchRecipesByName(
            SearchRecipesByNameRequest.newBuilder().setQuery("name").build()
        )
        return resp.recipesList
    }

    fun getInventory(): List<Item> {
//        val obs = InvPrinter()
        val resp = inventorySvc.getInventory(
            GetInventoryRequest.newBuilder().setUserId("name").build()
        )
        return resp.itemsList
    }
}