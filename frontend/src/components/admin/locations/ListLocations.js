import React, { Component } from "react";
import { Link } from "react-router-dom";
import { Table, Button } from "react-bootstrap";
import { Loading } from "../../shared/Loading";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMinus, faPlus } from "@fortawesome/free-solid-svg-icons";

function ShowDeletionError({ error }) {
  if (error) {
    return (
      <div>
        <h4>An error ocurred when deleting Location {error}</h4>
      </div>
    );
  } else {
    return <div></div>;
  }
}

function RenderData({ isLoading, error, locations, handleRemove }) {
  if (isLoading) {
    return (
      <div className="container">
        <div className="row">
          <Loading />
        </div>
      </div>
    );
  } else if (error) {
    return (
      <div>
        <h4>An error ocurred when feching Locations from server: {error}</h4>
      </div>
    );
  } else {
    return (
      <div className="container">
        <div className="row">
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>City</th>
                <th>Country</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {locations.map((loc) => {
                return (
                  <tr key={loc.uuid}>
                    <td>{loc.city}</td>
                    <td>{loc.country}</td>
                    <td>
                      <Button
                        variant="danger"
                        value={loc.uuid}
                        onClick={() => handleRemove(loc.uuid)}
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
    );
  }
}

class ListLocations extends Component {
  constructor(props) {
    super(props);
    this.handleRemove = this.handleRemove.bind(this);
  }

  handleRemove(uuid) {
    this.props.deleteLocation(uuid);
  }

  render() {
    return (
      <div className="container">
        <div>
          <h3 className="mt-4">Management of Locations</h3>
        </div>
        <Link to={`/newlocation`}>
          <button type="button" className="btn btn-success">
            <i>
              <FontAwesomeIcon icon={faPlus} />
            </i>{" "}
            New Location
          </button>
        </Link>
        <div>
          <RenderData
            isLoading={this.props.isLoading}
            error={this.props.errorListing}
            locations={this.props.locations}
            handleRemove={this.handleRemove}
          />
          <ShowDeletionError error={this.props.errorDeleting} />
        </div>
      </div>
    );
  }
}

export default ListLocations;
