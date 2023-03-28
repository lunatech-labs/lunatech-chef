import React, { useState } from 'react';
import { CDBInput } from 'cdbreact';
import Button from "react-bootstrap/Button";
import Container from "react-bootstrap/Container";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
export default function ClipboardCopy({ copyText }) {
    const [isCopied, setIsCopied] = useState(false);

    async function copyTextToClipboard(text) {
        if ('clipboard' in navigator) {
            return await navigator.clipboard.writeText(text);
        } else {
            document.execCommand('copy', true, text);
        }
    }

    // onClick handler function for the copy button
    const handleCopyClick = () => {
        // Asynchronously call copyTextToClipboard
        copyTextToClipboard(copyText)
            .then(() => {
                // If successful, update the isCopied state value
                setIsCopied(true);
                setTimeout(() => {
                    setIsCopied(false);
                }, 1500);
            })
            .catch((err) => {
                console.log(err);
            });
    }

    return (
        <div className>
            <Container>
                <Row>
                    <Col lg={9} md={12}>
                        <CDBInput type="text" value={copyText} disabled  background color={'info'} />
                    </Col>
                    <Col lg={3} md={12} >
                        <Button onClick={handleCopyClick}
                                type="button"
                                variant="info"
                                className="btn btn-sm btn-info">
                            <span>{isCopied ? 'Copied!' : 'Copy'}</span>
                        </Button>
                    </Col>
                </Row>
                <Row>
                    <Col lg={12} md={12} >
                       <p className="text-muted small">This token would only be shown once, make sure to copy</p>
                    </Col>
                </Row>
            </Container>
        </div>
    );
}