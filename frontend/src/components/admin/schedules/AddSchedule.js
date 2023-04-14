import React, { useState } from "react";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";
import { useNavigate } from "react-router-dom";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';

export default function AddSchedule(props) {
    function RenderData() {
        const [date, setDate] = useState(new Date());

        const handleChange = (date) => {
            setDate(date)
        };

        const navigate = useNavigate();
        const required = (value) => (value ? undefined : "Required");

        const onSubmit = (values) => {
            let shortDate = date.toISOString().substring(0, 10);
            let nextShortDate = date

            nextShortDate.setDate(date.getDate() + parseInt(values.recurrency))

            props.addNewSchedule({
                ...values,
                date: shortDate,
                nextDate: nextShortDate.toISOString().substring(0, 10)
            });
            navigate("/allschedules");
        };

        return (
            <Form
                onSubmit={onSubmit}
                initialValues={{
                    menuUuid: "",
                    officeUuid: "",
                    date: "", // not used, hook date used instead
                    recurrency: "0",
                }}
                render={({ handleSubmit, submitting, pristine }) => (
                    <form onSubmit={handleSubmit}>
                        <Row>
                            <Col lg="2">Choose menu:</Col>
                            <Col lg="3">
                                <div className="select">
                                    <Field validate={required} name="menuUuid" component="select">
                                        <option value="" />
                                        {props.menus.map((menu) => {
                                            return (
                                                <option value={menu.uuid} key={menu.uuid}>
                                                    {menu.name}
                                                </option>
                                            );
                                        })}
                                    </Field>
                                </div>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg="2">Choose office:</Col>
                            <Col lg="3">
                                <div className="select">
                                    <Field
                                        validate={required}
                                        name="officeUuid"
                                        component="select"
                                    >
                                        <option value="" />
                                        {props.offices.map((office) => {
                                            return (
                                                <option value={office.uuid} key={office.uuid}>
                                                    {office.city}, {office.country}
                                                </option>
                                            );
                                        })}
                                    </Field>
                                </div>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg="2">Choose date:</Col>
                            <Col lg="3">
                                <Field name="date" component="input">
                                    {() => (
                                        <div className="datePicker">
                                            <DatePicker
                                                selected={date}
                                                onChange={handleChange}
                                                dateFormat="dd-MM-yyyy"
                                            />
                                        </div>
                                    )}
                                </Field>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg="2">Repetition:</Col>
                            <Col lg="3">
                                <div className="select">
                                    <Field validate={required} name="recurrency" component="select">
                                        <option value="0" key="0">
                                            Single event
                                        </option>
                                        <option value="7" key="7">
                                            Every 7 days
                                        </option>
                                        <option value="14" key="14">
                                            Every 14 days
                                        </option>
                                        <option value="21" key="21">
                                            Every 21 days
                                        </option>
                                        <option value="28" key="28">
                                            Every 28 days
                                        </option>
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
                                        disabled={submitting || pristine}
                                    >
                                        Add Schedule
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
                <h3 className="mt-4 mb-4">New Schedule</h3>
            </div>
            <RenderData />
        </div>
    );
};
