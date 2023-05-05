import { createSlice } from '@reduxjs/toolkit'

const initState = {
    isLoading: false,
    offices: [],
    errorListing: null,
    errorAdding: null,
    errorEditing: null,
    errorDeleting: null,
};

// https://redux.js.org/usage/migrating-to-modern-redux
const officesSlice = createSlice({
    name: 'offices',
    initialState: initState,
    reducers: {
        allOfficesLoading(state, action) {
            state.isLoading = true
        },
        allOfficesShown(state, action) {
            state.isLoading = false;
            state.offices = action.payload.data
        },
        allOfficesLoadingFailed(state, action) {
            state.isLoading = false;
            state.errorListing = action.payload;
        },
        officeAddedFailed(state, action) {
            state.errorAdding = action.payload;
        },
        officeEditedFailed(state, action) {
            state.errorEditing = action.payload;
        },
        officeDeletedFailed(state, action) {
            state.errorDeleting = action.payload;
        },
    }
})

export const { allOfficesLoading, allOfficesShown, allOfficesLoadingFailed, officeAddedFailed, officeEditedFailed, officeDeletedFailed } = officesSlice.actions

export default officesSlice.reducer