import React from "react";
import { Form, Field } from "react-final-form";
import { useNavigate } from "react-router-dom";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';

export function AddOffice(props) {
    const navigate = useNavigate();
    const required = (value) => (value ? undefined : "Required");
    const onSubmit = (values) => {
        props.addNewOffice(values);
        navigate("/alloffices");
    };

    function RenderData() {
        return (
            <Form
                onSubmit={onSubmit}
                initialValues={{ city: "", country: "" }}
                render={({ handleSubmit, submitting, pristine }) => (
                    <form onSubmit={handleSubmit}>
                        <Row>
                            <Col lg="1">City</Col>
                            <Col lg="5">
                                <Field validate={required} name="city">
                                    {({ input, meta }) => (
                                        <div className="d-grid">
                                            <input {...input} type="text" placeholder="City" maxLength={50} />
                                            {meta.error && meta.touched &&
                                                <span className="text-danger">  {meta.error}</span>}
                                        </div>
                                    )}
                                </Field>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg="1">Country</Col>
                            <Col lg="5">
                                <Field validate={required} name="country">
                                    {({ input, meta }) => (
                                        <div className="d-grid">
                                            <input {...input} type="text" placeholder="Country" maxLength={50} />
                                            {meta.error && meta.touched &&
                                                <span className="text-danger">  {meta.error}</span>}
                                        </div>
                                    )}
                                </Field>
                            </Col>
                        </Row>
                        <Row className="mt-4">
                            <Col lg="6">
                                <div className="d-grid">
                                    <Button
                                        type="submit"
                                        variant="success"
                                        disabled={submitting || pristine}
                                    >
                                        Add Office
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
                <h3 className="mt-4 mb-4">New Office</h3>
            </div>
            <RenderData />
        </div>
    );
};
