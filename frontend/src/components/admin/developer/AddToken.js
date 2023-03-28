import React, {useEffect} from 'react';
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import ClipboardCopy from "../../shared/ClipboardCopy";
export default function AddToken(props){

    const { user } = props;
    const tokenText = user.token

    return (
        <div className="container">
            <Container>
                <Row>
                    <div>
                        <h3 className="mt-4 header ">API</h3>
                        <p className="py-2 subTitle text-bg-light p-4">Generate tokens for use with other Lunatech apps, like Lunatech LunchBot</p>
                    </div>
                </Row>
                <Row>
                    <Col lg={3} >
                        <Button
                            type="submit"
                            variant="success"
                            className="btn btn-sm btn-success"
                            onClick={() => props.handleTokenGeneration()}
                            disabled={ user?.isLoading}
                        >
                            Generate Token
                        </Button>
                    </Col>
                    <Col lg={9} md={12} >
                        {
                            tokenText && <ClipboardCopy copyText={tokenText} />
                        }

                    </Col>
                </Row>


                </Container>
        </div>
    )
}