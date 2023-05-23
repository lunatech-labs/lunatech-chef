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

    function RenderData() {
        return (
            <Row>
                <Form
                    onSubmit={onSubmit}
                    initialValues={{
                        officeUuid: props.user.officeUuid,
                        isVegetarian: props.user.isVegetarian,
                        hasHalalRestriction: props.user.hasHalalRestriction,
                        hasNutsRestriction: props.user.hasNutsRestriction,
                        hasSeafoodRestriction: props.user.hasSeafoodRestriction,
                        hasPorkRestriction: props.user.hasPorkRestriction,
                        hasBeefRestriction: props.user.hasBeefRestriction,
                        isGlutenIntolerant: props.user.isGlutenIntolerant,
                        isLactoseIntolerant: props.user.isLactoseIntolerant,
                        otherRestrictions: props.user.otherRestrictions,
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
                            <Row></Row>
                            <Row>
                                <Col className="fw-bold">
                                    ** Please use this space for real diet restrictions and not diet preferences **
                                </Col>
                            </Row>
                            <Row>
                                <Col lg="4">
                                    <Field name="isVegetarian" component="input" type="checkbox"></Field><span>  I'm vegetarian</span>
                                </Col>
                            </Row>
                            <Row>
                                <Col lg="4">
                                    <Field name="isGlutenIntolerant" component="input" type="checkbox"></Field><span>  I'm gluten intolerant</span>
                                </Col>
                            </Row>
                            <Row>
                                <Col lg="4">
                                    <Field name="isLactoseIntolerant" component="input" type="checkbox"></Field><span>  I'm lactose intolerant</span>
                                </Col>
                            </Row>
                            <Row>
                                <Col lg="4">
                                    <Field name="hasHalalRestriction" component="input" type="checkbox"></Field><span>  I only eat halal food</span>
                                </Col>
                            </Row>
                            <Row>
                                <Col lg="4">
                                    <Field name="hasNutsRestriction" component="input" type="checkbox"></Field><span>  I have a nuts allergy</span>
                                </Col>
                            </Row>
                            <Row>
                                <Col lg="4">
                                    <Field name="hasSeafoodRestriction" component="input" type="checkbox"></Field><span>  I have seafood allergy</span>
                                </Col>
                            </Row>
                            <Row>
                                <Col lg="4">
                                    <Field name="hasPorkRestriction" component="input" type="checkbox"></Field><span>  I don't eat pork</span>
                                </Col>
                            </Row>
                            <Row>
                                <Col lg="4">
                                    <Field name="hasBeefRestriction" component="input" type="checkbox"></Field><span>  I don't eat beef</span>
                                </Col>
                            </Row>
                            <Row>
                                <Col lg="2">Other restrictions:</Col>
                                <Col lg="5">
                                    <div className="d-grid">
                                        <Field name="otherRestrictions" component="textarea" lines="2"></Field>
                                    </div>
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
                    )
                    }
                ></Form>
            </Row>
        );
    }

    return (
        <Container>
            <Row>
                <h3 className="mt-4">User Profile</h3>
            </Row>
            {props.user.error ? <Alert key="danger" variant="danger">
                An error occured when saving the profile: {props.user.error}
            </Alert> : <div></div>}
            <RenderData />
        </Container>
    );
};
