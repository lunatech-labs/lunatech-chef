import { createSlice } from '@reduxjs/toolkit'

const initState = {
    isLoading: false,
    menus: [],
    errorListing: null,
    errorAdding: null,
    errorEditing: null,
    errorDeleting: null,
};

// https://redux.js.org/usage/migrating-to-modern-redux
const menusSlice = createSlice({
    name: 'menus',
    initialState: initState,
    reducers: {
        allMenusLoading(state, action) {
            state.isLoading = true
        },
        allMenusShown(state, action) {
            state.isLoading = false;
            state.menus = action.payload.data
        },
        allMenusLoadingFailed(state, action) {
            state.isLoading = false;
            state.errorListing = action.payload;
        },
        menuAddedFailed(state, action) {
            state.errorAdding = action.payload;
        },
        menuEditedFailed(state, action) {
            state.errorEditing = action.payload;
        },
        menuDeletedFailed(state, action) {
            state.errorDeleting = action.payload;
        },
    }
})

export const { allMenusLoading, allMenusShown, allMenusLoadingFailed, menuAddedFailed, menuEditedFailed, menuDeletedFailed } = menusSlice.actions

export default menusSlice.reducer