import React from "react";
import { Link, useNavigate } from "react-router-dom";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import Alert from 'react-bootstrap/Alert';
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus } from "@fortawesome/free-solid-svg-icons";

export default function ListMenus(props) {
    function ShowError({ error, reason }) {
        return (
            <Alert key="danger" variant="danger">
                An error occured when {reason} a menu: {error}
            </Alert>
        );
    }

    function RenderData({ isLoading, error, menus }) {
        if (isLoading) {
            return (
                <Row>
                    <Loading />
                </Row>
            );
        } else if (error) {
            return (
                <Alert key="danger" variant="danger">
                    An error occurred when feching Menus from server: {error}
                </Alert>
            );
        } else {
            return (
                <Row>
                    <Table striped bordered hover>
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Dishes</th>
                                <th></th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {menus.map((menu) => {
                                return (
                                    <tr key={menu.uuid}>
                                        <td>{menu.name}</td>
                                        <td>
                                            {menu.dishesNames.map((dish) => (
                                                <p key={dish}>{dish}</p>
                                            ))}
                                        </td>
                                        <td>
                                            <Button
                                                variant="primary"
                                                value={menu.uuid}
                                                onClick={() => handleEdit(menu)}
                                            >Edit</Button>
                                        </td>
                                        <td>
                                            <Button
                                                variant="danger"
                                                value={menu.uuid}
                                                onClick={() => handleRemove(menu.uuid)}
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

    const navigate = useNavigate();
    const handleEdit = (menu) => {
        navigate("/editmenu", { state: menu });
    }

    const handleRemove = (uuid) => {
        props.deleteMenu(uuid);
    }

    return (
        <Container>
            <Row>
                <h3 className="mt-4">Management of Menus</h3>
            </Row>
            <Row className="mb-5">
                <Link to="/newMenu">
                    <button type="button" className="btn btn-success">
                        <i>
                            <FontAwesomeIcon icon={faPlus} />
                        </i>{" "}
                        New Menu
                    </button>
                </Link>
            </Row>
            {props.errorAdding ? <ShowError error={props.errorAdding} reason="adding" /> : <div></div>}
            {props.errorDeleting ? <ShowError error={props.errorDeleting} reason="deleting" /> : <div></div>}
            {props.errorEditing ? <ShowError error={props.errorEditing} reason="saving" /> : <div></div>}
            <RenderData
                isLoading={props.isLoading}
                error={props.errorListing}
                menus={props.menus}
            />
        </Container>
    );
}

