import React, { useState } from "react";
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import Container from 'react-bootstrap/Container';
import Accordion from 'react-bootstrap/Accordion';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Alert from 'react-bootstrap/Alert';
import { Loading } from "./shared/Loading";
import { ToMonth } from "./shared/Functions";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";
import { STORAGE_FILTER_DATE_WHO_IS_JOINING, STORAGE_FILTER_OFFICE_WHO_IS_JOINING } from "../redux/LocalStorageKeys";

export default function WhoIsJoining(props) {
    const savedDate = localStorage.getItem(STORAGE_FILTER_DATE_WHO_IS_JOINING);
    const savedOffice = localStorage.getItem(STORAGE_FILTER_OFFICE_WHO_IS_JOINING);

    const [startDate, setDateSchedule] = useState(savedDate === null ? new Date() : new Date(savedDate));
    const [startOffice,] = useState(savedOffice === null ? "" : savedOffice);

    const handleDateChange = (date) => {
        setDateSchedule(date)
    };

    const handleFilter = (values) => {
        const shortDate = startDate.toISOString().substring(0, 10);

        const chosenOffice =
            values.office === undefined ? "" : values.office;

        localStorage.setItem(STORAGE_FILTER_DATE_WHO_IS_JOINING, shortDate);
        localStorage.setItem(STORAGE_FILTER_OFFICE_WHO_IS_JOINING, chosenOffice);
        props.filterAttendance(shortDate, values.office);
        props.filterExternalAttendance(shortDate, values.office);
    };

    const handleExternalAttendanceUpdate = (values) => {
        props.editExternalAttendance(values);
    }

    function RenderData({
        isLoading,
        error,
        attendances,
        externalAttendances,
        offices,
        isUserAdmin
    }) {
        if (isLoading) {
            return (
                <Row>
                    <Loading />
                </Row>
            );
        } else if (error) {
            return (
                <Alert key="danger" variant="danger">
                    An error occurred when feching attendances from server: {error}
                </Alert>
            );
        } else {
            return (
                <div>
                    <Row>
                        <div className="shadow-sm p-3 mb-5 bg-white rounded">
                            <Form
                                onSubmit={handleFilter}
                                initialValues={{
                                    office: startOffice,
                                    date: "", // not used. startDate used instead
                                }}
                                render={({ handleSubmit, submitting }) => (
                                    <form onSubmit={handleSubmit}>
                                        <Row>
                                            <Col lg="1">
                                                <label>Office:</label>
                                            </Col>
                                            <Col lg="3">
                                                <div className="select">
                                                    <Field name="office" component="select" md="auto">
                                                        <option value="" key="" />
                                                        {offices.map((office) => {
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
                                            <Col lg="1">
                                                <label>Date:</label>
                                            </Col>
                                            <Col lg="3">
                                                <Field name="date" component="input">
                                                    {({ input, meta }) => (
                                                        <div className="datePicker">
                                                            <DatePicker
                                                                selected={startDate}
                                                                onChange={handleDateChange}
                                                                dateFormat="dd-MM-yyyy"
                                                            />
                                                            {meta.error && meta.touched && (
                                                                <span className="text-danger">  {meta.error}</span>
                                                            )}
                                                        </div>
                                                    )}
                                                </Field>
                                            </Col>
                                        </Row>
                                        <Row>
                                            <Col lg="4">
                                                <div className="d-grid">
                                                    <Button variant="info" type="submit" disabled={submitting}>
                                                        Filter
                                                    </Button>
                                                </div>
                                            </Col>
                                        </Row>
                                    </form>
                                )}
                            ></Form>
                        </div>
                    </Row>
                    {isUserAdmin ? (
                        <div>
                            <h1>External Attendees</h1>
                            <Table striped bordered className="table-externalAttendances">
                                <thead>
                                    <tr>
                                        <td width={240}>Menu</td>
                                        <td width={230}>Office</td>
                                        <td width={230}>Date</td>
                                        <td>Total number of external attendants</td>
                                    </tr>
                                </thead>
                                <tbody>
                                    {externalAttendances.map((externalAttendance, index) => {
                                        return (
                                            <tr key={index}>
                                                <td>{externalAttendance.menu.name}</td>
                                                <td>{externalAttendance.office}</td>
                                                <td>{externalAttendance.date[2]} {ToMonth(externalAttendance.date[1])} {externalAttendance.date[0]}</td>
                                                <td>
                                                    <Form
                                                        onSubmit={handleExternalAttendanceUpdate}
                                                        initialValues={{
                                                            uuid: externalAttendance.uuid,
                                                            attendancesCount: externalAttendance.attendancesCount
                                                        }}
                                                        render={({ handleSubmit, submitting, pristine }) => (
                                                            <form onSubmit={handleSubmit}>
                                                                <div className="input-group">
                                                                    <Field name="attendancesCount" >
                                                                        {({ input }) => (
                                                                            <div className="d-grid">
                                                                                <input {...input} type="number" maxLength={20} min={0} max={9999} />
                                                                            </div>
                                                                        )}
                                                                    </Field>

                                                                    <Button
                                                                        type="submit"
                                                                        variant="success"
                                                                        disabled={submitting}
                                                                    >
                                                                        Save
                                                                    </Button>
                                                                </div>
                                                            </form>
                                                        )}
                                                    >
                                                        <Button
                                                            variant="primary"
                                                            onClick={() => handleSave(externalCount)}
                                                        >Save</Button>
                                                    </Form>
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </Table>
                            <Row></Row>
                            <h1>Lunatech Attendees</h1>
                        </div>
                    ) : <></>}
                    <Table striped bordered className="table-whoisjoining">
                        <thead>
                            <tr>
                                <td width={240}>Menu</td>
                                <td width={230}>Office</td>
                                <td width={230}>Date</td>
                                <td>Total number of attendants</td>
                            </tr>
                        </thead>
                    </Table>
                    <Accordion>
                        {attendances.map((attendance) => {
                            return (
                                <Accordion.Item eventKey={attendance.uuid} key={attendance.uuid}>
                                    <Accordion.Header>
                                        <Col>{attendance.menuName}</Col>
                                        <Col>{attendance.office}</Col>
                                        <Col>{attendance.date[2]} {ToMonth(attendance.date[1])} {attendance.date[0]}</Col>
                                        <Col>{attendance.attendants.length}</Col>
                                    </Accordion.Header>
                                    <Accordion.Body>
                                        <Table striped bordered hover>
                                            <tbody>
                                                {attendance.attendants.map((attendant) => {
                                                    return (
                                                        <tr key={attendant.uuid}>
                                                            <td><b>{attendant.name} </b></td>
                                                            {attendant.isVegetarian ? (<td>Vegetarian</td>) : ("")}
                                                            {attendant.hasHalalRestriction ? (<td>Halal only</td>) : ("")}
                                                            {attendant.hasNutsRestriction ? (<td>Nuts allergy</td>) : ("")}
                                                            {attendant.hasSeafoodRestriction ? (
                                                                <td>Seafood allergy</td>) : ("")}
                                                            {attendant.hasPorkRestriction ? (<td>No pork</td>) : ("")}
                                                            {attendant.hasBeefRestriction ? (<td>No beef</td>) : ("")}
                                                            {attendant.isGlutenIntolerant ? (
                                                                <td>Gluten intolerant</td>) : ("")}
                                                            {attendant.isLactoseIntolerant ? (
                                                                <td>Lactose intolerant</td>) : ("")}
                                                            {attendant.otherRestrictions ? (
                                                                <td>{attendant.otherRestrictions}</td>) : ("")}
                                                        </tr>
                                                    );
                                                })}
                                            </tbody>
                                        </Table>
                                    </Accordion.Body>
                                </Accordion.Item>
                            );
                        })}
                    </Accordion>
                </div >
            );
        }
    }

    return (
        <Container>
            <Row>
                <h3 className="mt-4">Who's joining?</h3>
            </Row>
            <RenderData
                isLoading={props.isLoading}
                error={props.errorListing}
                attendances={props.attendance}
                externalAttendances={props.externalAttendance}
                offices={props.offices}
                isUserAdmin={props.isUserAdmin}
            />
        </Container>
    );
}
