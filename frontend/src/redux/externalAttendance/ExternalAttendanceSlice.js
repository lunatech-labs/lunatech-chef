import { createSlice } from '@reduxjs/toolkit'

const initState = {
    isLoading: false,
    errorEditing: null,
};

const externalAttendancesSlice = createSlice({
    name: 'externalAttendances',
    initialState: initState,
    reducers: {
        externalAttendanceEditedFailed(state, action) {
            state.errorEditing = action.payload;
        },
    }
})

export const { externalAttendanceEditedFailed } = externalAttendancesSlice.actions

export default externalAttendancesSlice.reducer