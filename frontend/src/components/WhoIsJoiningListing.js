import React from "react";
import Table from "react-bootstrap/Table";

export const WhoIsJoiningListing = (props) => {
  function RenderData({ attendances }) {
    return (
      <div className="container">
        <div className="row">
          <Table striped bordered hover>
            <tbody>
              {attendances.map((name) => {
                return (
                  <tr key={name}>
                    <td>{name}</td>
                  </tr>
                );
              })}
            </tbody>
          </Table>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div>
        <h3 className="mt-4">Who is joining?</h3>
      </div>
      <div>
        <RenderData attendances={props.listAttendants} />
      </div>
    </div>
  );
};
