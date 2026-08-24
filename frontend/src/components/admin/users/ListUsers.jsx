import React from "react";
import { useNavigate } from "react-router-dom";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import Alert from 'react-bootstrap/Alert';
import { Loading } from "../../shared/Loading";

export default function ListUsers(props) {
    function ShowError({ error, reason }) {
        if (error) {
            return (
                <Alert key="danger" variant="danger">
                    An error occurred when {reason} an employee: {error}
                </Alert>
            );
        }
    }

    const officeCity = (officeUuid) => {
        const office = props.offices.find((office) => office.uuid === officeUuid);
        return office ? office.city : "";
    };

    const restrictionLabels = [
        ["isVegetarian", "Vegetarian"],
        ["isGlutenIntolerant", "Gluten intolerant"],
        ["isLactoseIntolerant", "Lactose intolerant"],
        ["hasHalalRestriction", "Halal"],
        ["hasNutsRestriction", "Nuts allergy"],
        ["hasSeafoodRestriction", "Seafood allergy"],
        ["hasPorkRestriction", "No pork"],
        ["hasBeefRestriction", "No beef"],
    ];

    const restrictionsSummary = (user) => {
        const labels = restrictionLabels
            .filter(([field]) => user[field])
            .map(([, label]) => label);
        if (user.otherRestrictions) {
            labels.push(user.otherRestrictions);
        }
        return labels.join(", ");
    };

    function RenderData({ isLoading, error, users }) {
        if (isLoading) {
            return (
                <Row>
                    <Loading />
                </Row>
            );
        } else if (error) {
            return (
                <Alert key="danger" variant="danger">
                    An error occurred when fetching Employees from server: {error}
                </Alert>
            );
        } else {
            return (
                <Row>
                    <Table striped bordered hover>
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>E-mail</th>
                                <th>Office</th>
                                <th>Restrictions</th>
                                <th>Lunches</th>
                                <th></th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map((user) => {
                                return (
                                    <tr key={user.uuid}>
                                        <td>{user.name}</td>
                                        <td>{user.emailAddress}</td>
                                        <td>{officeCity(user.officeUuid)}</td>
                                        <td>{restrictionsSummary(user)}</td>
                                        <td>{user.optOutLunches ? "Opted out" : ""}</td>
                                        <td>
                                            <Button
                                                variant="primary"
                                                value={user.uuid}
                                                onClick={() => handleEdit(user)}
                                            >Edit</Button>
                                        </td>
                                        <td>
                                            <Button
                                                variant="danger"
                                                value={user.uuid}
                                                onClick={() => handleRemove(user.uuid)}
                                            >Delete</Button>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </Table>
                </Row>
            );
        }
    }

    const handleRemove = (uuid) => {
        props.deleteUser(uuid);
    }

    const navigate = useNavigate();
    const handleEdit = (user) => {
        navigate("/edituser", { state: user });
    }

    return (
        <Container>
            <Row>
                <h3 className="mt-4">Management of Employees</h3>
            </Row>
            {props.errorDeleting ? <ShowError error={props.errorDeleting} reason="deleting" /> : null}
            {props.errorEditing ? <ShowError error={props.errorEditing} reason="saving" /> : null}
            <RenderData
                isLoading={props.isLoading}
                error={props.errorListing}
                users={props.users}
            />
        </Container>
    );
}
