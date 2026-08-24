import React from "react";
import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { vi } from "vitest";
import { ConfigureStore } from "../../../redux/ConfigureStore";
import { axiosInstance } from "../../../redux/Axios";
import Main from "../../Main";

const mockUseAuth = vi.fn();

vi.mock("react-oidc-context", () => ({
    useAuth: () => mockUseAuth(),
    hasAuthParams: () => false,
}));

vi.mock("../../../redux/Axios", () => ({
    axiosInstance: {
        get: vi.fn(),
        put: vi.fn(),
        post: vi.fn(),
        delete: vi.fn(),
    },
    onUnauthorized: vi.fn(),
}));

const adminMe = {
    uuid: "admin-1",
    name: "Admin User",
    emailAddress: "admin.user@lunatech.nl",
    isAdmin: true,
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

const offices = [
    { uuid: "office-1", city: "Rotterdam", country: "Netherlands" },
    { uuid: "office-2", city: "Amsterdam", country: "Netherlands" },
];

const employees = [
    {
        uuid: "user-1",
        name: "Jane Doe",
        emailAddress: "jane.doe@lunatech.nl",
        officeUuid: "office-2",
        isVegetarian: true,
        hasHalalRestriction: false,
        hasNutsRestriction: false,
        hasSeafoodRestriction: false,
        hasPorkRestriction: false,
        hasBeefRestriction: false,
        isGlutenIntolerant: false,
        isLactoseIntolerant: false,
        otherRestrictions: "",
        optOutLunches: false,
    },
    {
        uuid: "user-2",
        name: "John Smith",
        emailAddress: "john.smith@lunatech.nl",
        officeUuid: null,
        isVegetarian: false,
        hasHalalRestriction: false,
        hasNutsRestriction: false,
        hasSeafoodRestriction: false,
        hasPorkRestriction: false,
        hasBeefRestriction: false,
        isGlutenIntolerant: false,
        isLactoseIntolerant: false,
        otherRestrictions: "",
        optOutLunches: true,
    },
    {
        uuid: "user-3",
        name: "Gone Person",
        emailAddress: "gone.person@lunatech.nl",
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
        isInactive: true,
    },
];

const renderAt = (path) =>
    render(
        <Provider store={ConfigureStore()}>
            <MemoryRouter initialEntries={[path]}>
                <Main />
            </MemoryRouter>
        </Provider>
    );

beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        activeNavigator: undefined,
    });
    axiosInstance.get.mockImplementation((url) => {
        if (url === "/me") return Promise.resolve({ data: adminMe });
        if (url === "/users") return Promise.resolve({ data: employees });
        if (url === "/offices") return Promise.resolve({ data: offices });
        return Promise.resolve({ data: [] });
    });
});

test("deleting an employee removes it via the API", async () => {
    axiosInstance.delete.mockResolvedValue({});
    renderAt("/allusers");

    const row = (await screen.findByText("John Smith")).closest("tr");
    await userEvent.click(within(row).getByRole("button", { name: "Delete" }));

    expect(axiosInstance.delete).toHaveBeenCalledWith("/users/user-2");
});

test("editing an employee saves the changed profile via the API", async () => {
    axiosInstance.put.mockResolvedValue({});
    renderAt("/allusers");

    const row = (await screen.findByText("Jane Doe")).closest("tr");
    await userEvent.click(within(row).getByRole("button", { name: "Edit" }));

    expect(await screen.findByText("Editing Employee")).toBeInTheDocument();
    await userEvent.click(screen.getByRole("checkbox", { name: /gluten intolerant/i }));
    await userEvent.click(screen.getByRole("button", { name: "Save Employee" }));

    expect(axiosInstance.put).toHaveBeenCalledWith("/users/user-1", {
        officeUuid: "office-2",
        isVegetarian: true,
        hasHalalRestriction: false,
        hasNutsRestriction: false,
        hasSeafoodRestriction: false,
        hasPorkRestriction: false,
        hasBeefRestriction: false,
        isGlutenIntolerant: true,
        isLactoseIntolerant: false,
        otherRestrictions: "",
        optOutLunches: false,
    });
    expect(await screen.findByText("Management of Employees")).toBeInTheDocument();
});

test("admin reaches the employees list from the sidebar", async () => {
    renderAt("/");

    await userEvent.click(await screen.findByText("Employees"));

    expect(await screen.findByText("Management of Employees")).toBeInTheDocument();
});

test("non-admin users do not get an Employees entry in the sidebar", async () => {
    axiosInstance.get.mockImplementation((url) =>
        Promise.resolve({ data: url === "/me" ? { ...adminMe, isAdmin: false } : [] })
    );
    renderAt("/");

    expect(await screen.findByText("Logout")).toBeInTheDocument();
    expect(screen.queryByText("Employees")).not.toBeInTheDocument();
});

test("the list shows dietary restrictions and opt-out status", async () => {
    renderAt("/allusers");

    const jane = (await screen.findByText("Jane Doe")).closest("tr");
    expect(within(jane).getByText("Vegetarian")).toBeInTheDocument();

    const john = (await screen.findByText("John Smith")).closest("tr");
    expect(within(john).getByText("Opted out")).toBeInTheDocument();
});

test("inactive employees are hidden by default", async () => {
    renderAt("/allusers");

    expect(await screen.findByText("Jane Doe")).toBeInTheDocument();
    expect(screen.queryByText("Gone Person")).not.toBeInTheDocument();
});

test("toggling show inactive reveals inactive employees with a badge", async () => {
    renderAt("/allusers");

    await screen.findByText("Jane Doe");
    await userEvent.click(screen.getByRole("checkbox", { name: /show inactive/i }));

    const row = (await screen.findByText("Gone Person")).closest("tr");
    expect(within(row).getByText("Inactive")).toBeInTheDocument();
});

test("admin sees the list of employees with name, email and office", async () => {
    renderAt("/allusers");

    expect(await screen.findByText("Jane Doe")).toBeInTheDocument();
    expect(screen.getByText("jane.doe@lunatech.nl")).toBeInTheDocument();
    expect(screen.getByText("Amsterdam")).toBeInTheDocument();
    expect(screen.getByText("John Smith")).toBeInTheDocument();
});
