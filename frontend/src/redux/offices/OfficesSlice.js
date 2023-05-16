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
            state.errorListing = null;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = null;
        },
        allOfficesShown(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.offices = action.payload
        },
        allOfficesLoadingFailed(state, action) {
            state.isLoading = false;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.errorListing = action.payload;
        },
        officeAddedFailed(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.errorAdding = action.payload;
        },
        officeEditedFailed(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorAdding = null;
            state.errorDeleting = null;
            state.errorEditing = action.payload;
        },
        officeDeletedFailed(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = action.payload;
        },
    }
})

export const { allOfficesLoading, allOfficesShown, allOfficesLoadingFailed, officeAddedFailed, officeEditedFailed, officeDeletedFailed } = officesSlice.actions

export default officesSlice.reducer