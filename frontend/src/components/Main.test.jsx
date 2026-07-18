import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { vi } from "vitest";
import { ConfigureStore } from "../redux/ConfigureStore";
import { axiosInstance } from "../redux/Axios";
import Main from "./Main";

const mockUseAuth = vi.fn();

vi.mock("react-oidc-context", () => ({
    useAuth: () => mockUseAuth(),
    hasAuthParams: () => false,
}));

vi.mock("../redux/Axios", () => ({
    axiosInstance: {
        get: vi.fn(),
        put: vi.fn(),
        post: vi.fn(),
        delete: vi.fn(),
    },
    onUnauthorized: vi.fn(),
}));

const me = {
    uuid: "user-1",
    name: "Normal User",
    emailAddress: "normal.user@lunatech.nl",
    isAdmin: false,
    officeUuid: "office-1",
    isVegetarian: false,
    hasHalalRestriction: false,
    hasNutsRestriction: false,
    hasSeafoodRestriction: false,
    hasPorkRestriction: false,
    hasBeefRestriction: false,
    isGlutenIntolerant: false,
    isLactoseIntolerant: false,
    otherRestrictions: "",
    optOutLunches: false,
};

const renderMain = () =>
    render(
        <Provider store={ConfigureStore()}>
            <MemoryRouter initialEntries={["/"]}>
                <Main />
            </MemoryRouter>
        </Provider>
    );

beforeEach(() => {
    vi.clearAllMocks();
    axiosInstance.get.mockImplementation((url) =>
        Promise.resolve({ data: url === "/me" ? me : [] })
    );
});

test("restores the app session on page refresh when an OIDC session is still present", async () => {
    // Simulates a page refresh: react-oidc-context restored the user from
    // storage, but the Redux store starts from its initial state.
    mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        activeNavigator: undefined,
    });

    renderMain();

    await waitFor(() => expect(axiosInstance.get).toHaveBeenCalledWith("/me"));
    expect(await screen.findByText("Logout")).toBeInTheDocument();
});

test("logging out removes the OIDC user and returns to the login page", async () => {
    const authState = {
        isAuthenticated: true,
        isLoading: false,
        activeNavigator: undefined,
        removeUser: vi.fn(() => {
            authState.isAuthenticated = false;
            return Promise.resolve();
        }),
    };
    mockUseAuth.mockImplementation(() => ({ ...authState }));

    renderMain();

    await userEvent.click(await screen.findByText("Logout"));

    expect(authState.removeUser).toHaveBeenCalled();
    expect(
        await screen.findByRole("button", { name: "Sign in" })
    ).toBeInTheDocument();
});

test("shows the login page when there is no OIDC session", () => {
    mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        activeNavigator: undefined,
    });

    renderMain();

    expect(screen.getByRole("button", { name: "Sign in" })).toBeInTheDocument();
    expect(axiosInstance.get).not.toHaveBeenCalledWith("/me");
});
