package com.example.coba.domain.usecase

import com.example.coba.domain.repository.FileRepository
import javax.inject.Inject

class SetSessionPasswordUseCase @Inject constructor(
    private val repository: FileRepository
) {
    operator fun invoke(password: String?) = repository.setSessionPassword(password)
}
