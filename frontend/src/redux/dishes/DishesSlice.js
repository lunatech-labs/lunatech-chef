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
            state.isLoading = true
        },
        allDishesShown(state, action) {
            state.isLoading = false;
            state.dishes = action.payload.data
        },
        allDishesLoadingFailed(state, action) {
            state.isLoading = false;
            state.errorListing = action.payload;
        },
        dishAddedFailed(state, action) {
            state.errorAdding = action.payload;
        },
        dishAEditedFailed(state, action) {
            state.errorEditing = action.payload;
        },
        dishDeletedFailed(state, action) {
            state.errorDeleting = action.payload;
        },
    }
})

export const { allDishesShown, allDishesLoading, allDishesLoadingFailed, dishAddedFailed, dishAEditedFailed, dishDeletedFailed } = dishesSlice.actions

export default dishesSlice.reducer