package com.ubertob.unlearnoop.zettai.fp


typealias CommandHandler<COMMAND, EVENT, ERROR> = (COMMAND) -> Outcome<ERROR, List<EVENT>>
