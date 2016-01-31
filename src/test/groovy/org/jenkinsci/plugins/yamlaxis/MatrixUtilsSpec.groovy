package org.jenkinsci.plugins.yamlaxis

class MatrixUtilsSpec extends spock.lang.Specification {
    def "contain"(){
        expect:
        MatrixUtils.contains(parent, child) == expected

        where:
        parent                | child                 | expected
        ["a":1, "b":2, "c":3] | ["a":1, "b":2, "c":3] | true
        ["a":1, "b":2, "c":3] | ["a":1, "c":3]        | true
        ["a":1, "b":2, "c":3] | ["a":1, "c":4]        | false
        ["a":1, "b":2, "c":3] | ["a":1, "c":3, "d":4] | false
    }

    def "reject"(){
        setup:
        def variables = [
                ["a":1,  "b":2, "c":3],
                ["a":11, "b":2, "c":3],
                ["a":21, "b":2, "c":3],
        ]

        expect:
        MatrixUtils.reject(variables, excludes) == expected

        where:
        excludes                 || expected
        [["b":2, "a":11, "c":3]] || [["a":1, "b":2, "c":3],  ["a":21, "b":2, "c":3]]
        [["a":1]]                || [["a":11, "b":2, "c":3], ["a":21, "b":2, "c":3]]
        [["b":2]]                || []
    }
}
