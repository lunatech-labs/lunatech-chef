import React from "react";
import { Form, Field } from "react-final-form";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';
import Container from 'react-bootstrap/Container';

export default function MonthlyReports(props) {
    const handleSubmit = (values) => {
        props.getReport({
            ...values
        });
    };

    function range(size, startAt = 0) {
        return [...Array(size).keys()].map(i => i + startAt);
    }

    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    const initialYear = 2023; // year of app deployment
    const yearRange = range(currentYear - initialYear + 1, initialYear);
    const currentMonth = currentDate.getMonth() + 1;

    return (
        <Container>
            <div>
                <h3 className="mt-4 mb-4">Monthly reports</h3>
            </div>
            <div>
                <Row>
                    <Form
                        onSubmit={handleSubmit}
                        initialValues={{
                            year: currentYear,
                            month: currentMonth,
                        }}
                        render={({ handleSubmit }) => (
                            <form onSubmit={handleSubmit}>
                                <Row>
                                    <Col lg="2">Choose year:</Col>
                                    <Col lg="3">
                                        <div className="select">
                                            <Field name="year" component="select" md="auto">
                                                {yearRange.map((year) => {
                                                    return (
                                                        <option value={year} key={year}>{year}</option>
                                                    );
                                                })}
                                            </Field>
                                        </div>
                                    </Col>
                                </Row>
                                <Row>
                                    <Col lg="2">Choose month:</Col>
                                    <Col lg="3">
                                        <div className="select">
                                            <Field
                                                name="month"
                                                component="select"
                                            >
                                                <option value="1" key="1">January</option>
                                                <option value="2" key="2">February</option>
                                                <option value="3" key="3">March</option>
                                                <option value="4" key="4">April</option>
                                                <option value="5" key="5">May</option>
                                                <option value="6" key="6">June</option>
                                                <option value="7" key="7">July</option>
                                                <option value="8" key="8">August</option>
                                                <option value="9" key="9">September</option>
                                                <option value="10" key="10">October</option>
                                                <option value="11" key="11">November</option>
                                                <option value="12" key="12">December</option>
                                            </Field>
                                        </div>
                                    </Col>
                                </Row>
                                <Row className="mt-4">
                                    <Col lg="5">
                                        <div className="d-grid">
                                            <Button
                                                type="submit"
                                                variant="success"
                                            >
                                                Download report
                                            </Button>
                                        </div>
                                    </Col>
                                </Row>
                            </form>
                        )
                        }
                    ></Form >
                </Row>
            </div >
        </Container >
    );
};