import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import Container from 'react-bootstrap/Container';
import Accordion from 'react-bootstrap/Accordion';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import { Loading } from "./shared/Loading";
import { ToMonth } from "./shared/Functions";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";

export default function WhoIsJoining(props) {
  const savedDate = localStorage.getItem("filterDateWhoIsJoining");
  const savedLocation = localStorage.getItem("filterLocationWhoIsJoining");

  const [startDate, setDateSchedule] = useState(savedDate === null ? new Date() : new Date(savedDate));
  const [startLocation, _] = useState(savedLocation === null ? "" : savedLocation);

  const handleDateChange = (date) => {
    setDateSchedule(date)
  };

  const handleFilter = (values) => {
    const shortDate = startDate.toISOString().substring(0, 10);

    const choosenLocation =
      values.location === undefined ? "" : values.location;

    localStorage.setItem("filterDateWhoIsJoining", shortDate);
    localStorage.setItem("filterLocationWhoIsJoining", choosenLocation);
    props.filter(shortDate, values.location);
  };

  const navigate = useNavigate();
  const handleDetails = (attendants) => {
    navigate("/whoisjoininglisting", { state: attendants });
  }

  function RenderData({
    isLoading,
    error,
    attendances,
    locations,
  }) {
    if (isLoading) {
      return (
        <Row>
          <Loading />
        </Row>
      );
    } else if (error) {
      return (
        <Row>
          <h4>An error ocurred when feching attendances from server: {error}</h4>
        </Row>
      );
    } else {
      return (
        <div>
          <Row>
            <Form
              onSubmit={handleFilter}
              initialValues={{
                location: startLocation,
                date: "", // not used. used instead
              }}
              render={({ handleSubmit, submitting }) => (
                <form onSubmit={handleSubmit}>
                  <Row>
                    <Col lg="2">
                      <label>Location:</label>
                    </Col>
                    <Col lg="3">
                      <div className="select">
                        <Field name="location" component="select" md="auto">
                          <option value="" key="" />
                          {locations.map((location) => {
                            return (
                              <option value={location.uuid} key={location.uuid}>
                                {location.city}, {location.country}
                              </option>
                            );
                          })}
                        </Field>
                      </div>
                    </Col>
                  </Row>
                  <Row>
                    <Col lg="2">
                      <label>Date:</label>
                    </Col>
                    <Col lg="3">
                      <Field name="date" component="input" >
                        {({ input, meta }) => (
                          <div className="datePicker" >
                            <DatePicker
                              selected={startDate}
                              onChange={handleDateChange}
                              dateFormat="dd-MM-yyyy"
                            />
                            {meta.error && meta.touched && (
                              <span>{meta.error}</span>
                            )}
                          </div>
                        )}
                      </Field>
                    </Col>
                  </Row>
                  <Row>
                    <Col lg="5">
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
          </Row >
          <Row></Row>
          <Table striped bordered>
            <thead>
              <tr key="head">
                <td>Menu</td>
                <td>Location</td>
                <td>Date</td>
                <td>Total attendants</td>
              </tr>
            </thead>
          </Table>
          <Accordion>
            {attendances.map((attendance) => {
              return (
                <Accordion.Item eventKey={attendance.uuid}>
                  <Accordion.Header>
                    <Col>{attendance.menuName}</Col>
                    <Col>{attendance.location.city}, {attendance.location.country}</Col>
                    <Col>{attendance.date[2]} {ToMonth(attendance.date[1])} {attendance.date[0]}</Col>
                    <Col>{attendance.attendants.length}</Col>
                  </Accordion.Header>
                  <Accordion.Body>
                    <Table striped bordered hover>
                      <tbody>
                        {attendance.attendants.map((name) => {
                          return (
                            <tr key={name}>
                              <td>{name}</td>
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
        <h3 className="mt-4">Who is joining?</h3>
      </Row>
      <RenderData
        isLoading={props.isLoading}
        error={props.errorListing}
        attendances={props.attendance}
        locations={props.locations}
      />
    </Container>
  );
}
