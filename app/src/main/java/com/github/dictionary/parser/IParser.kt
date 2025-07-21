package com.github.dictionary.parser

interface IParser {
    fun parse(path: String): List<ParsedResult>
}






