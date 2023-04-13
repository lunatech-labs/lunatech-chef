import * as ActionTypes from "./OfficesActionTypes";

const initState = {
    isLoading: false,
    offices: [],
    errorListing: null,
    errorAdding: null,
    errorEditing: null,
    errorDeleting: null,
};

export const OfficesReducer = (state = initState, action) => {
    switch (action.type) {
        case ActionTypes.SHOW_ALL_OFFICES:
            return {...initState, offices: action.payload};

        case ActionTypes.OFFICES_LOADING:
            return {...initState, isLoading: true};

        case ActionTypes.OFFICES_LOADING_FAILED:
            return {
                ...initState,
                errorListing: action.payload,
                errorAdding: null,
                errorEditing: null,
                errorDeleting: null,
            };

        case ActionTypes.ADD_NEW_OFFICE_FAILED:
            return {
                ...state,
                errorListing: null,
                errorAdding: action.payload,
                errorEditing: null,
                errorDeleting: null,
            };

        case ActionTypes.EDIT_OFFICE_FAILED:
            return {
                ...state,
                errorListing: null,
                errorAdding: null,
                errorEditing: action.payload,
                errorDeleting: null,
            };

        case ActionTypes.DELETE_OFFICE_FAILED:
            return {
                ...state,
                errorListing: null,
                errorAdding: null,
                errorEditing: null,
                errorDeleting: action.payload,
            };

        default:
            return state;
    }
};
