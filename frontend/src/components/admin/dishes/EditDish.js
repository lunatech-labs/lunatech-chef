import React from "react";
import { Form, Field } from "react-final-form";
import { useNavigate, useLocation } from "react-router-dom";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';

export function EditDish(props) {
    const navigate = useNavigate();
    const required = (value) => (value ? undefined : "Required");

    const dish = useLocation().state;

    const onSubmit = (values) => {
        let editedDish = {
            ...values,
            uuid: dish.uuid,
        };
        props.editDish(editedDish);
        navigate("/alldishes");
    };

    function ShowError({ error }) {
        if (error) {
            return (
                <div>
                    <h4>An error occurred when editing a Dish: {error}</h4>
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
                    name: dish.name,
                    description: dish.description,
                    isVegetarian: dish.isVegetarian,
                    isHalal: dish.isHalal,
                    hasNuts: dish.hasNuts,
                    hasSeafood: dish.hasSeafood,
                    hasPork: dish.hasPork,
                    hasBeef: dish.hasBeef,
                    isGlutenFree: dish.isGlutenFree,
                    isLactoseFree: dish.isLactoseFree,
                }}
                render={({ handleSubmit, submitting }) => (
                    <form onSubmit={handleSubmit}>
                        <Row>
                            <Col lg="1">Name</Col>
                            <Col lg="5">
                                <Field validate={required} name="name">
                                    {({ input, meta }) => (
                                        <div className="d-grid">
                                            <input {...input} placeholder="Name" maxLength={60} />
                                            {meta.error && meta.touched &&
                                                <span className="text-danger">  {meta.error}</span>}
                                        </div>
                                    )}
                                </Field>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg="1">Details</Col>
                            <Col lg="5">
                                <Field name="description">
                                    {({ input, meta }) => (
                                        <div className="d-grid">
                                            <textarea {...input} placeholder="Description just for admins" maxLength={200} size="2" />
                                            {meta.error && meta.touched &&
                                                <span className="text-danger">  {meta.error}</span>}
                                        </div>
                                    )}
                                </Field>
                            </Col>
                        </Row>
                        <Row className="mt-4">
                            <Col lg="4">Choose all that apply:</Col>
                        </Row>
                        <Row>
                            <Col lg="1">
                                <Field
                                    name="isVegetarian"
                                    component="input"
                                    type="checkbox"
                                ></Field>
                            </Col>
                            <Col lg="2">Is vegetarian</Col>
                        </Row>
                        <Row>
                            <Col lg="1">
                                <Field name="isHalal" component="input" type="checkbox"></Field>
                            </Col>
                            <Col lg="2">Is halal</Col>
                        </Row>
                        <Row>
                            <Col lg="1">
                                <Field name="hasNuts" component="input" type="checkbox"></Field>
                            </Col>
                            <Col lg="2">Contains nuts</Col>
                        </Row>
                        <Row>
                            <Col lg="1">
                                <Field
                                    name="hasSeafood"
                                    component="input"
                                    type="checkbox"
                                ></Field>
                            </Col>
                            <Col lg="2">Contains seafood</Col>
                        </Row>
                        <Row>
                            <Col lg="1">
                                <Field name="hasPork" component="input" type="checkbox"></Field>
                            </Col>
                            <Col lg="2">Contains pork</Col>
                        </Row>
                        <Row>
                            <Col lg="1">
                                <Field name="hasBeef" component="input" type="checkbox"></Field>
                            </Col>
                            <Col lg="2">Contains beef</Col>
                        </Row>
                        <Row>
                            <Col lg="1">
                                <Field
                                    name="isGlutenFree"
                                    component="input"
                                    type="checkbox"
                                ></Field>
                            </Col>
                            <Col lg="2">Is gluten free</Col>
                        </Row>
                        <Row>
                            <Col lg="1">
                                <Field
                                    name="isLactoseFree"
                                    component="input"
                                    type="checkbox"
                                ></Field>
                            </Col>
                            <Col lg="2">Is lactose free</Col>
                        </Row>
                        <Row className="mt-4">
                            <Col lg="5">
                                <div className="d-grid">
                                    <Button
                                        type="submit"
                                        variant="success"
                                        disabled={submitting}
                                    >
                                        Save Dish
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
                <h3 className="mt-4 mb-4">Editing Dish</h3>
            </div>
            <RenderData />
            <ShowError error={props.error} />
        </div>
    );
};
