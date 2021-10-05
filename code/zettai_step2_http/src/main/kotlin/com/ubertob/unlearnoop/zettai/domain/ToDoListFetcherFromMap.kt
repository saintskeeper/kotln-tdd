package com.ubertob.unlearnoop.zettai.domain


typealias ToDoListStore = MutableMap<User, MutableMap<ListName, ToDoList>>

data class ToDoListFetcherFromMap(
    private val store: ToDoListStore
) : ToDoListFetcher {
    override fun invoke(user: User, listName: ListName): ToDoList? =
        store[user]?.get(listName)

    fun addListToUser(user: User, list: ToDoList) {
        store.compute(user) { _, value ->
            val listMap = value ?: mutableMapOf()
            listMap.apply { put(list.listName, list) }
        }
    }
}
