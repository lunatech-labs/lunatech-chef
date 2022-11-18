import React from "react";
import Table from "react-bootstrap/Table";
import { useLocation } from "react-router-dom";

export const WhoIsJoiningListing = () => {
  const attendances = useLocation().state;

  function RenderData() {
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
        <RenderData />
      </div>
    </div>
  );
};
