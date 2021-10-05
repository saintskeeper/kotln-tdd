package com.ubertob.unlearnoop.zettai.domain

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ToDoList?
}

typealias ToDoListFetcher = (User, ListName) -> ToDoList?

class ToDoListHub(val fetcher: ToDoListFetcher) : ZettaiHub {

    override fun getList(user: User, listName: ListName): ToDoList? =
        fetcher(user, listName)

}

