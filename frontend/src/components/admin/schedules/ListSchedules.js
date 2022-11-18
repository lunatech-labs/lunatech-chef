import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
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
        <h4>
          An error ocurred when {reason} a schedule: {error}
        </h4>
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
    <div className="container">
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
          <div className="container">
            <div className="row">
              <Loading />
            </div>
          </div>
        ) : (
          <div></div>
        )}
        {props.errorListing ? (
          <div>
            <h4>
              An error ocurred when fetching Schedules from server:{" "}
              {props.errorListing}
            </h4>
          </div>
        ) : (
          <div className="container">
            <div>
              <h4 className="row">Schedule of Menus</h4>
              <h5 className="mt-4">Where and when a menu will be served</h5>
            </div>
            <div className="row">
              <label>Filter by:</label>
            </div>
            <div className="row">
              <Form
                onSubmit={handleFilter}
                initialValues={{
                  location: startLocationSchedule,
                  date: "", // not used, startDateSchedule used instead
                }}
                render={({ handleSubmit, submitting }) => (
                  <form onSubmit={handleSubmit}>
                    <div className="row">
                      <div className="column">
                        <label>Location:</label>
                        <Field name="location" component="select">
                          <option value="" key="" />
                          {props.locations.map((location) => {
                            return (
                              <option
                                value={location.uuid}
                                key={location.uuid}
                              >
                                {location.city}, {location.country}
                              </option>
                            );
                          })}
                        </Field>
                      </div>
                      <div className="column">
                        <Field name="date" component="input">
                          {({ input, meta }) => (
                            <div>
                              <label>Date:</label>
                              <DatePicker
                                selected={startDateSchedule}
                                onChange={handleDateChange}
                                dateFormat="dd-MM-yyyy"
                              />
                              {meta.error && meta.touched && (
                                <span>{meta.error}</span>
                              )}
                            </div>
                          )}
                        </Field>
                      </div>
                      <div>
                        <button
                          type="submit"
                          color="primary"
                          disabled={submitting}
                        >
                          Filter
                        </button>
                      </div>
                    </div>
                  </form>
                )}
              ></Form>
            </div>
            <div className="row">
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
            </div>
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
          <div className="container">
            <div className="row">
              <Loading />
            </div>
          </div>
        ) : (
          <div></div>
        )}
        <div className="container">
          <div className="row">
            <label>Filter by:</label>
          </div>
          <div className="row">
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
          </div>
        </div>
      </div>
    </div>
  );
}
