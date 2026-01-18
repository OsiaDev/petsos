package com.osia.petsos.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.osia.petsos.data.repository.PetRepositoryImpl
import com.osia.petsos.domain.repository.PetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providePetRepository(repository: PetRepositoryImpl): PetRepository = repository

    @Provides
    @Singleton
    fun provideAuthRepository(repository: com.osia.petsos.data.repository.AuthRepositoryImpl): com.osia.petsos.domain.repository.AuthRepository = repository

}
