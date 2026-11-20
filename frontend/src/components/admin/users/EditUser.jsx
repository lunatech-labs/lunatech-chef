import React from "react";
import { Form, Field } from "react-final-form";
import { useNavigate, useLocation, Navigate } from "react-router-dom";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';

export function EditUser(props) {
    const user = useLocation().state;
    const navigate = useNavigate();

    if (!user) return <Navigate to="/allusers" replace />;
    const onSubmit = (values) => {
        const editedUser = {
            ...values,
            uuid: user.uuid,
        };
        props.editUser(editedUser);
        navigate("/allusers");
    };

    function ShowError({ error }) {
        if (error) {
            return (
                <div>
                    <h4>An error occurred when editing an Employee: {error}</h4>
                </div>
            );
        } else {
            return null;
        }
    }

    function RenderData() {
        return (
            <Form
                onSubmit={onSubmit}
                initialValues={{
                    officeUuid: user.officeUuid,
                    optOutLunches: user.optOutLunches,
                }}
                render={({ handleSubmit, submitting }) => (
                    <form onSubmit={handleSubmit}>
                        <Row>
                            <Col lg="2">Name: </Col> <Col lg="4">{user.name}</Col>
                        </Row>
                        <Row>
                            <Col lg="2">E-mail: </Col> <Col lg="4">{user.emailAddress}</Col>
                        </Row>
                        <Row>
                            <Col lg="2">Prefered office: </Col>
                            <Col lg="4">
                                <div className="select">
                                    <Field name="officeUuid" component="select">
                                        <option value="" />
                                        {props.offices.map((office) => {
                                            return (
                                                <option value={office.uuid} key={office.uuid}>
                                                    {office.city}
                                                </option>
                                            );
                                        })}
                                    </Field>
                                </div>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg="4">
                                <label>
                                    <Field name="optOutLunches" component="input" type="checkbox"></Field>
                                    <span>  Opted out from all lunches</span>
                                </label>
                            </Col>
                        </Row>
                        <Row className="mt-4">
                            <Col lg="7">
                                <div className="d-grid">
                                    <Button
                                        type="submit"
                                        variant="success"
                                        disabled={submitting}
                                    >
                                        Save Employee
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
                <h3 className="mt-4 mb-4">Editing Employee</h3>
            </div>
            <RenderData />
            <ShowError error={props.error} />
        </div>
    );
};
