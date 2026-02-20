import React, { useState } from "react";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";
import { useNavigate, useLocation, Navigate } from "react-router-dom";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';

export default function EditSchedule(props) {
    function ShowError({ error }) {
        if (error) {
            return (
                <div>
                    <h4>An error occurred when editing a Schedule: {error}</h4>
                </div>
            );
        } else {
            return null;
        }
    }

    function RenderData({ isRecurrent }) {
        return (
            <Form
                onSubmit={onSubmit}
                initialValues={{
                    menuUuid: schedule.menu.uuid,
                    officeUuid: schedule.office.uuid,
                    date: date,
                    recurrency: isRecurrent ? schedule.repetitionDays : "0",
                }}
                render={({ handleSubmit, submitting }) => (
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
                                    <Field validate={required} name="officeUuid" component="select">
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
                        {!isRecurrent /** Only modify date for single schedules  */ ? (
                            <Row>
                                <Col lg="2">Choose date:</Col>
                                <Col lg="3">
                                    <Field name="date" component="input">
                                        {({ meta }) => (
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
                        ) : (
                            <Row></Row>
                        )}
                        {isRecurrent /** A single schedule cannot be made recurrent  */ ? (
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
                        ) : (
                            <Row></Row>
                        )}
                        <Row className="mt-4">
                            <Col lg="5">
                                <div className="d-grid">
                                    <Button
                                        type="submit"
                                        variant="success"
                                        disabled={submitting}
                                    >
                                        Save Schedule
                                    </Button>
                                </div>
                            </Col>
                        </Row>
                    </form>
                )}
            ></Form>
        );
    }

    const required = (value) => (value ? undefined : "Required");

    const getInitialDate = (schedule) => {
        if (!schedule) return new Date();
        if ("date" in schedule) {
            return new Date(schedule.date[0], schedule.date[1] - 1, schedule.date[2]);
        } else {
            return new Date(schedule.nextDate[0], schedule.nextDate[1] - 1, schedule.nextDate[2]);
        }
    };

    const navigate = useNavigate();
    const schedule = useLocation().state;

    const [date, setDate] = useState(new Date(getInitialDate(schedule)));

    if (!schedule) return <Navigate to="/allschedules" replace />;

    const handleChange = (selectedDate) => {
        setDate(selectedDate)
    };
    const onSubmit = (values) => {
        let shortDate = date.toISOString().substring(0, 10);
        let editedSchedule = {
            ...values,
            uuid: schedule.uuid,
            date: shortDate,
        };
        // filterDate to refresh fetchSchedules
        props.editSchedule(editedSchedule);
        navigate("/allschedules");
    };

    return (
        <div className="container">
            <div>
                <h3 className="mt-4 mb-4">Editing Schedule</h3>
            </div>
            <RenderData
                isRecurrent={"repetitionDays" in schedule ? true : false}
            />
            <ShowError error={props.error} />
        </div>
    );
};
