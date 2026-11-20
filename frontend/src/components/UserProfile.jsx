import React from "react";
import { Form, Field } from "react-final-form";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Alert from 'react-bootstrap/Alert';
import Button from 'react-bootstrap/Button';

export const UserProfile = (props) => {
    const onSubmit = (values) => {
        props.saveUserProfile(props.user.uuid, values);
    };

    return (
        <Container>
            <Row>
                <h3 className="mt-4">User Profile</h3>
            </Row>
            {props.user.error ? (
                <Alert key="danger" variant="danger">
                    An error occurred when saving the profile: {props.user.error}
                </Alert>
            ) : null}
            <Row>
                <Form
                    onSubmit={onSubmit}
                    initialValues={{
                        officeUuid: props.user.officeUuid,
                        optOutLunches: props.user.optOutLunches,
                    }}
                    render={({ handleSubmit, submitting, pristine }) => (
                        <form onSubmit={handleSubmit}>
                            <Row>
                                {" "}
                                <Col lg="2">Name: </Col> <Col lg="3">{props.user.name}</Col>
                            </Row>
                            <Row>
                                {" "}
                                <Col lg="2">E-mail: </Col> <Col lg="3">{props.user.emailAddress}</Col>
                            </Row>
                            <Row>
                                <Col lg="2">Prefered office: </Col>
                                <Col lg="3">
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
                                    <Field name="optOutLunches" component="input" type="checkbox"></Field><span>  I want to opt out from all lunches</span>
                                </Col>
                            </Row>
                            <Row className="mt-4">
                                <Col lg="7">
                                    <div className="d-grid">
                                        <Button
                                            type="submit"
                                            variant="primary"
                                            disabled={submitting || pristine}
                                        >
                                            Save
                                        </Button>
                                    </div>
                                </Col>
                            </Row>
                        </form>
                    )}
                ></Form>
            </Row>
        </Container>
    );
};
