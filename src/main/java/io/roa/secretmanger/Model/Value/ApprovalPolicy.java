package io.roa.secretmanger.Model.Value;

public enum ApprovalPolicy {
    RELAXED,   // any 1 approver — suitable for non-critical config files
    STANDARD,  // any 2 approvers — default for most credentials
    STRICT     // team lead + PM + 1 admin all required — production secrets
}