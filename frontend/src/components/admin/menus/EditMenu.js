import React from "react";
import { Form, Field } from "react-final-form";
import { useNavigate, useLocation } from "react-router-dom";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';

export function EditMenu(props) {
    const required = (value) => (value ? undefined : "Required");

    const menu = useLocation().state;

    const navigate = useNavigate();
    const onSubmit = (values) => {
        let editedMenu = {
            ...values,
            uuid: menu.uuid,
        };
        props.editMenu(editedMenu);
        navigate("/allmenus");
    };

    function ShowError({ error }) {
        if (error) {
            return (
                <div>
                    <h4>An error occurred when editing a Menu: {error}</h4>
                </div>
            );
        } else {
            return <div></div>;
        }
    }

    function RenderData() {
        return (
            <Form
                onSubmit={onSubmit}
                initialValues={{
                    name: menu.name,
                    dishesUuids: menu.dishes.map((dish) => dish.uuid),
                }}
                render={({ handleSubmit, submitting }) => (
                    <form onSubmit={handleSubmit}>
                        <Row>
                            <Col lg="1">Name</Col>
                            <Col lg="5">
                                <Field validate={required} name="name">
                                    {({ input, meta }) => (
                                        <div className="d-grid">
                                            <input {...input} type="text" placeholder="Name" maxLength={50} />
                                            {meta.error && meta.touched && <span>{meta.error}</span>}
                                        </div>
                                    )}
                                </Field>
                            </Col>
                        </Row>
                        <Row> <Col lg="6"> <h6 className="mt-4 ">Edit dishes in the menu:</h6></Col></Row>
                        {props.dishes.map((dish) => {
                            return (
                                <Row key={dish.uuid}>
                                    <Col lg="6">
                                        <Field
                                            name="dishesUuids"
                                            component="input"
                                            type="checkbox"
                                            value={dish.uuid}
                                        ></Field> {dish.name}
                                    </Col>
                                </Row>
                            );
                        })}
                        <Row className="mt-4">
                            <Col lg="6">
                                <div className="d-grid">
                                    <Button
                                        type="submit"
                                        variant="success"
                                        disabled={submitting}
                                    >
                                        Save Menu
                                    </Button>
                                </div>
                            </Col>
                        </Row>
                    </form>
                )}
            ></Form>
        );
    }

    return (
        <div className="container">
            <div>
                <h3 className="mt-4 mb-4">Editing Menu</h3>
            </div>
            <RenderData />
            <ShowError error={props.error} />
        </div>
    );
};
