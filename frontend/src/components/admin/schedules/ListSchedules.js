import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Container from 'react-bootstrap/Container';
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus, faEdit } from "@fortawesome/free-solid-svg-icons";
import { ToMonth } from "../../shared/Functions";
import { Form, Field } from "react-final-form";
import DatePicker from "react-datepicker";

export default function ListSchedules(props) {

  function ShowError({ error, reason }) {
    if (error) {
      return (
        <Row>
          An error ocurred when {reason} a schedule: {error}
        </Row>
      );
    } else {
      return <div></div>;
    }
  }

  const savedDate = localStorage.getItem("filterDateSchedule");
  const savedLocationSchedule = localStorage.getItem(
    "filterLocationSchedule"
  );

  const [startDateSchedule, setDateSchedule] = useState(savedDate === null ? new Date() : new Date(savedDate));
  const [startLocationSchedule, _] = useState(savedLocationSchedule === null ? "" : savedLocationSchedule);

  const handleDateChange = (date) => {
    setDateSchedule(date)
  };

  const navigate = useNavigate();
  const handleEdit = (schedule) => {
    navigate("/editschedule", { state: schedule });
  }

  const handleRemoveSchedule = (uuid) => {
    props.deleteSchedule(uuid);
  };

  const handleRemoveRecurrentSchedule = (uuid) => {
    props.deleteRecurrentSchedule(uuid);
  };

  const handleFilter = (values) => {
    const shortDate = startDateSchedule
      .toISOString()
      .substring(0, 10);

    const choosenLocation =
      values.location === undefined ? "" : values.location;

    localStorage.setItem("filterDateSchedule", shortDate);
    localStorage.setItem("filterLocationSchedule", choosenLocation);
    props.filterSchedules();
    props.filterRecurrentSchedules();
  };

  return (
    <Container>
      <div>
        <h3 className="mt-4">Management of Scheduled Menus</h3>
      </div>
      <Link to={`/newSchedule`}>
        <button type="button" className="btn btn-success">
          <i>
            <FontAwesomeIcon icon={faPlus} />
          </i>{" "}
          New Schedule
        </button>
      </Link>
      <div>
        <ShowError error={props.errorAdding} reason="adding" />
        <ShowError error={props.errorDeleting} reason="deleting" />
        <ShowError error={props.errorEditing} reason="saving" />
        {props.isLoading ? (
          <Row>
            <Loading />
          </Row>
        ) : (
          <div></div>
        )}
        {props.errorListing ? (
          <Row>
            <h4>
              An error ocurred when fetching Schedules from server:{" "}
              {props.errorListing}
            </h4>
          </Row>
        ) : (
          <div>

            <Row>
              <Form
                onSubmit={handleFilter}
                initialValues={{
                  location: startLocationSchedule,
                  date: "", // not used, startDateSchedule used instead
                }}
                render={({ handleSubmit, submitting }) => (
                  <form onSubmit={handleSubmit}>
                    <Row>
                      <Col lg="2">Location:
                      </Col>
                      <Col lg="3">
                        <div className="select">
                          <Field name="location" component="select" md="auto">
                            <option value="" key="" />
                            {props.locations.map((location) => {
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
                      <Col lg="2">Date:
                      </Col>
                      <Col lg="3">
                        <Field name="date" component="input" >
                          {({ input, meta }) => (
                            <div className="datePicker" >
                              <DatePicker
                                selected={startDateSchedule}
                                onChange={handleDateChange}
                                dateFormat="dd-MM-yyyy"
                              />
                              {meta.error && meta.touched && (<span className="text-danger">  {meta.error}</span>
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
            </Row>
            <Row>
              <Table striped bordered hover>
                <thead>
                  <tr>
                    <th>Menu</th>
                    <th>Location</th>
                    <th>Date</th>
                    <th></th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {props.schedules.map((schedule) => {
                    return (
                      <tr key={schedule.uuid}>
                        <td>{schedule.menu.name}</td>
                        <td>
                          {schedule.location.city},{" "}
                          {schedule.location.country}
                        </td>
                        <td>
                          {schedule.date[2]} {ToMonth(schedule.date[1])}{" "}
                          {schedule.date[0]}
                        </td>
                        <td>
                          <Button
                            variant="primary"
                            value={schedule.uuid}
                            onClick={() => handleEdit(schedule)}
                          >
                            <FontAwesomeIcon icon={faEdit} />
                          </Button>
                        </td>
                        <td>
                          <Button
                            variant="danger"
                            value={schedule.uuid}
                            onClick={() => handleRemoveSchedule(schedule.uuid)
                            }
                          >
                            <FontAwesomeIcon icon={faMinus} />
                          </Button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </Table>
            </Row>
          </div>
        )}
      </div>
      <div>
        <h4 className="mt-4">Recurrent Schedules</h4>
        <h5 className="mt-4">
          A list of menus that will be scheduled automatically based on it's
          recurrency
        </h5>
      </div>
      <div>
        <ShowError error={props.errorAdding} reason="adding" />
        <ShowError error={props.errorDeleting} reason="deleting" />
        <ShowError error={props.errorEditing} reason="saving" />
        {props.isLoading ? (
          <Row>
            <Loading />
          </Row>
        ) : (
          <div></div>
        )}
        <Row>
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>Menu</th>
                <th>Location</th>
                <th>Recurrency (days)</th>
                <th>Next date</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {props.recurrentSchedules.map((recurrentSchedule) => {
                return (
                  <tr key={recurrentSchedule.uuid}>
                    <td>{recurrentSchedule.menu.name}</td>
                    <td>
                      {recurrentSchedule.location.city},{" "}
                      {recurrentSchedule.location.country}
                    </td>
                    <td>{recurrentSchedule.repetitionDays}</td>
                    <td>
                      {" "}
                      {recurrentSchedule.nextDate[2]}{" "}
                      {ToMonth(recurrentSchedule.nextDate[1])}{" "}
                      {recurrentSchedule.nextDate[0]}
                    </td>
                    <td>
                      <Button
                        variant="primary"
                        value={recurrentSchedule.uuid}
                        onClick={() => handleEdit(recurrentSchedule)}
                      >
                        <FontAwesomeIcon icon={faEdit} />
                      </Button>
                    </td>
                    <td>
                      <Button
                        variant="danger"
                        value={recurrentSchedule.uuid}
                        onClick={() => handleRemoveRecurrentSchedule(recurrentSchedule.uuid)}
                      >
                        <FontAwesomeIcon icon={faMinus} />
                      </Button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </Table>
        </Row>
      </div>
    </Container>
  );
}
