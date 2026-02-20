import React from "react";
import { Loading } from "../../shared/Loading";
import { Link, useNavigate } from "react-router-dom";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import Alert from 'react-bootstrap/Alert';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus, faCheck } from "@fortawesome/free-solid-svg-icons";

export default function ListDishes(props) {

    function ShowError({ error, reason }) {
        return (
            <Alert key="danger" variant="danger">
                An error occured when {reason} a dish: {error}
            </Alert>
        );
    }

    function RenderData({ isLoading, error, dishes }) {
        if (isLoading) {
            return (
                <Row>
                    <Loading />
                </Row>
            );
        } else if (error) {
            return (
                <Alert key="danger" variant="danger">
                    An error occurred when feching Dishes from server: {error}
                </Alert>
            );
        } else {
            return (
                <Row>
                    <Table striped bordered hover>
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Details</th>
                                <th>Vegetarian</th>
                                <th>Halal</th>
                                <th>Nuts</th>
                                <th>Seafood</th>
                                <th>Pork</th>
                                <th>Beef</th>
                                <th>Gluten free</th>
                                <th>Lactose free</th>
                                <th></th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {dishes.map((dish) => {
                                return (
                                    <tr key={dish.uuid}>
                                        <td>{dish.name}</td>
                                        <td>{dish.description}</td>
                                        <td>
                                            {dish.isVegetarian ? (
                                                <span>
                                                    <FontAwesomeIcon icon={faCheck} />
                                                </span>
                                            ) : (
                                                ""
                                            )}
                                        </td>
                                        <td>
                                            {dish.isHalal ? (
                                                <span>
                                                    <FontAwesomeIcon icon={faCheck} />
                                                </span>
                                            ) : (
                                                ""
                                            )}
                                        </td>
                                        <td>
                                            {dish.hasNuts ? (
                                                <span>
                                                    <FontAwesomeIcon icon={faCheck} />
                                                </span>
                                            ) : (
                                                ""
                                            )}
                                        </td>
                                        <td>
                                            {dish.hasSeafood ? (
                                                <span>
                                                    <FontAwesomeIcon icon={faCheck} />
                                                </span>
                                            ) : (
                                                ""
                                            )}
                                        </td>
                                        <td>
                                            {dish.hasPork ? (
                                                <span>
                                                    <FontAwesomeIcon icon={faCheck} />
                                                </span>
                                            ) : (
                                                ""
                                            )}
                                        </td>
                                        <td>
                                            {dish.hasBeef ? (
                                                <span>
                                                    <FontAwesomeIcon icon={faCheck} />
                                                </span>
                                            ) : (
                                                ""
                                            )}
                                        </td>
                                        <td>
                                            {dish.isGlutenFree ? (
                                                <span>
                                                    <FontAwesomeIcon icon={faCheck} />
                                                </span>
                                            ) : (
                                                ""
                                            )}
                                        </td>
                                        <td>
                                            {dish.isLactoseFree ? (
                                                <span>
                                                    <FontAwesomeIcon icon={faCheck} />
                                                </span>
                                            ) : (
                                                ""
                                            )}
                                        </td>
                                        <td>
                                            <Button
                                                variant="primary"
                                                value={dish.uuid}
                                                onClick={() => handleEdit(dish)}
                                            >Edit</Button>
                                        </td>
                                        <td>
                                            <Button
                                                variant="danger"
                                                value={dish.uuid}
                                                onClick={() => handleRemove(dish.uuid)}
                                            > Delete</Button>
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
        props.deleteDish(uuid);
    }

    const navigate = useNavigate();
    const handleEdit = (dish) => {
        navigate("/editdish", { state: dish });
    }

    return (
        <Container>
            <Row>
                <h3 className="mt-4">Management of Dishes</h3>
            </Row>
            <Row className="mb-5">
                <Col xs="auto">
                    <Button as={Link} to="/newdish" variant="success">
                        <FontAwesomeIcon icon={faPlus} />{" "}
                        New Dish
                    </Button>
                </Col>
            </Row>
            {props.errorAdding ? <ShowError error={props.errorAdding} reason="adding" /> : null}
            {props.errorDeleting ? <ShowError error={props.errorDeleting} reason="deleting" /> : null}
            {props.errorEditing ? <ShowError error={props.errorEditing} reason="saving" /> : null}
            <RenderData
                isLoading={props.isLoading}
                error={props.errorListing}
                dishes={props.dishes}
            />

        </Container>
    );
}
