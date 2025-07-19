package com.github.dictionary.repository

import com.github.dictionary.db.DictDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictRepository @Inject constructor(private val dao: DictDao) : DictDao by dao {


}
