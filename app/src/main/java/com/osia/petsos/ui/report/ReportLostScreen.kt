package com.osia.petsos.ui.report

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.osia.petsos.domain.model.AdvertisementType

@Composable
fun ReportLostScreen(
    onBackClick: () -> Unit,
    viewModel: ReportPetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setReportType(AdvertisementType.LOST)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBackClick() // Or navigate to detail/home
        }
    }

    ReportPetForm(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onBreedChange = viewModel::onBreedChange,
        onCategoryChange = viewModel::onCategoryChange,
        onImagesSelected = viewModel::onImagesSelected,
        onImageRemoved = viewModel::onImageRemoved,
        onLocationSelected = viewModel::onLocationSelected,
        onRewardChanged = viewModel::onRewardChanged,
        onRewardAmountChanged = viewModel::onRewardAmountChanged,
        onContactNameChange = viewModel::onContactNameChange,
        onContactPhoneChange = viewModel::onContactPhoneChange,
        onSubmit = viewModel::submitReport,
        onBackClick = onBackClick,
        onErrorDismiss = viewModel::errorShown
    )
}
