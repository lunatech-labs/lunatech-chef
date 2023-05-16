import React from "react";
import { Link, useNavigate } from "react-router-dom";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus } from "@fortawesome/free-solid-svg-icons";

export default function ListOffice(props) {
    function ShowError({ error, reason }) {
        if (error) {
            return (
                <Alert key="danger" variant="danger">
                    An error occured when {reason} an office: {error}
                </Alert>
            );
        }
    }

    function RenderData({ isLoading, error, offices }) {
        if (isLoading) {
            return (
                <Row>
                    <Loading />
                </Row>
            );
        } else if (error) {
            return (
                <Alert key="danger" variant="danger">
                    An error occurred when feching Offices from server: {error}
                </Alert>
            );
        } else {
            return (
                <Row>
                    <Table striped bordered hover>
                        <thead>
                            <tr>
                                <th>City</th>
                                <th>Country</th>
                                <th></th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {offices.map((loc) => {
                                return (
                                    <tr key={loc.uuid}>
                                        <td>{loc.city}</td>
                                        <td>{loc.country}</td>
                                        <td>
                                            <Button
                                                variant="primary"
                                                value={loc.uuid}
                                                onClick={() => handleEdit(loc)}
                                            >Edit</Button>
                                        </td>
                                        <td>
                                            <Button
                                                variant="danger"
                                                value={loc.uuid}
                                                onClick={() => handleRemove(loc.uuid)}
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
        props.deleteOffice(uuid);
    }

    const navigate = useNavigate();
    const handleEdit = (office) => {
        navigate("/editoffice", { state: office });
    }

    return (
        <Container>
            <Row>
                <h3 className="mt-4">Management of Offices</h3>
            </Row>
            <Row className="mb-5">
                <Link to="/newoffice">
                    <button type="button" className="btn btn-success">
                        <i>
                            <FontAwesomeIcon icon={faPlus} />
                        </i>{" "}
                        New Office
                    </button>
                </Link>
            </Row>
            {props.errorAdding ? <ShowError error={props.errorAdding} reason="adding" /> : <div></div>}
            {props.errorDeleting ? <ShowError error={props.errorDeleting} reason="deleting" /> : <div></div>}
            {props.errorEditing ? <ShowError error={props.errorEditing} reason="saving" /> : <div></div>}
            <RenderData
                isLoading={props.isLoading}
                error={props.errorListing}
                offices={props.offices}
            />
        </Container>
    );
}
