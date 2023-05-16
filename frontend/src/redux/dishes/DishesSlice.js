import { createSlice } from '@reduxjs/toolkit'

const initState = {
    isLoading: false,
    dishes: [],
    errorListing: null,
    errorAdding: null,
    errorEditing: null,
    errorDeleting: null,
};

// https://redux.js.org/usage/migrating-to-modern-redux
const dishesSlice = createSlice({
    name: 'dishes',
    initialState: initState,
    reducers: {
        allDishesLoading(state, action) {
            state.errorListing = null;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.isLoading = true
        },
        allDishesShown(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.dishes = action.payload
        },
        allDishesLoadingFailed(state, action) {
            state.isLoading = false;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.errorListing = action.payload;
        },
        dishAddedFailed(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.errorAdding = action.payload;
        },
        dishEditedFailed(state, action) {
            state.isLoading = false;
            state.errorAdding = null;
            state.errorListing = null;
            state.errorDeleting = null;
            state.errorEditing = action.payload;
        },
        dishDeletedFailed(state, action) {
            state.isLoading = false;
            state.errorAdding = null;
            state.errorListing = null;
            state.errorEditing = null;
            state.errorDeleting = action.payload;
        },
    }
})

export const { allDishesLoading, allDishesShown, allDishesLoadingFailed, dishAddedFailed, dishEditedFailed, dishDeletedFailed } = dishesSlice.actions

export default dishesSlice.reducer